package controllers

import java.io._
import java.util.UUID
import javax.inject._

import controllers.dto.PipelineGrouping
import dao.ExecutionResultDao
import models.ExecutionResult
import org.apache.jena.query.DatasetFactory
import org.apache.jena.rdf.model.Model
import org.apache.jena.riot.{Lang, RDFDataMgr}
import play.Logger
import play.api.Configuration
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import play.api.libs.json._
import play.api.mvc._
import services.DiscoveryService
import services.discovery.model.components.DataSourceInstance
import services.discovery.model.{DataSample, Pipeline}
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}
import scalaj.http.{Http, MultiPart}
import services.discovery.model.etl.EtlPipeline


@Singleton
class DiscoveryController @Inject()(
    service: DiscoveryService,
    configuration: Configuration,
    executionResultDao: ExecutionResultDao,
    protected val dbConfigProvider: DatabaseConfigProvider
)(implicit executionContext: ExecutionContext) extends InjectedController with HasDatabaseConfigProvider[JdbcProfile] {

    val discoveryLogger = Logger.of("discovery")
    private val ldcpEndpointUri = configuration.get[String]("ldcp.endpointUri")
    private val templateSourceUri = configuration.get[String]("ldcp.templateSourceUri")

    def listComponents = Action {
        Ok(
            service.listTemplates(templateSourceUri) match {
                case Right(input) => Json.toJson(input)
                case Left(e) => businessError(s"Error while downloading template data: ${e.getMessage}")
            }
        )
    }

    private def businessError(errorMessage: String) = Json.toJson(Json.obj("error" -> Json.obj("message" -> JsString(errorMessage))))

    def startExperiment = Action(parse.json) { request =>
        val uris = request.body.as[Seq[String]]
        val discoveryId = service.runExperiment(uris, Map())
        Ok(Json.obj("id" -> Json.toJson(discoveryId)))
    }

    def startExperimentFromInputIri(iri: String) = Action {
        val discoveryId = service.runExperimentFromInputIri(iri)
        Ok(Json.obj("id" -> Json.toJson(discoveryId)))
    }

    def getExperimentsInputIrisFromIri(iri: String) = Action {
        val inputIris = service.getExperimentsInputIrisFromIri(iri)
        Ok(Json.obj("inputIris" -> inputIris.map(ii => Json.toJson(ii))))
    }

    def getExperimentsInputIris = Action { request: Request[AnyContent] =>
        val body: AnyContent = request.body
        val inputIris = service.getExperimentsInputIris(body.asText.getOrElse(""))
        Ok(Json.obj("inputIris" -> inputIris.map(ii => Json.toJson(ii))))
    }

    def startExperimentFromInput = Action { request: Request[AnyContent] =>
        val body: AnyContent = request.body
        val discoveryId = service.runExperimentFromInput(body.asText.getOrElse(""))
        Ok(Json.obj("id" -> Json.toJson(discoveryId)))
    }

    def status(id: String) = Action {
        val result = service.getStatus(id)
        Ok(Json.toJson(result))
    }

    def list(id: String) = Action {
        val maybePipelines = service.getPipelinesOfDiscovery(id)
        Ok(Json.obj(
            "pipelines" -> maybePipelines.map { pipelines =>
                pipelines.map { p =>
                    Json.obj(
                        "id" -> p._1.toString,
                        "componentCount" -> JsNumber(p._2.components.size),
                        "dataSources" -> Json.arr(p._2.components.filter(_.componentInstance.isInstanceOf[DataSourceInstance]).map(d => Json.obj(
                            "label" -> d.componentInstance.asInstanceOf[DataSourceInstance].label
                        ))),
                        "visualizer" -> p._2.lastComponent.componentInstance.getClass.getSimpleName
                    )
                }
            }
        ))
    }

    def pipelineGroups(id: String) = Action {
        Ok(service.getPipelinesOfDiscovery(id).map { pipelineMap =>
            JsObject(Seq("pipelineGroups" -> Json.toJson(PipelineGrouping.create(pipelineMap))))
        }.getOrElse(JsObject(Seq())))
    }

    def sampleEquals(ds1: DataSample, ds2: DataSample): Boolean = {
        val uuid = UUID.randomUUID()
        val d = ds1.getModel(uuid, 0).difference(ds2.getModel(uuid, 0))
        d.isEmpty
    }

    def minIteration(pipelines: Seq[Pipeline]): Int = {
        pipelines.map(p => p.lastComponent.discoveryIteration).min
    }

    def getSparqlService(discoveryId: String, pipelineId: String) = Action.async { r =>
        executionResultDao.findByPipelineId(discoveryId, pipelineId).map {
            case Some(executionResult) => {
                TurtleResponse(
                    service.getService(executionResult, r.host, ldcpEndpointUri)
                )
            }
            case _ => NotFound
        }
    }

    def getDataSampleSparqlService(discoveryId: String, pipelineId: String) = Action.async { r =>
        Future.successful(service.withPipeline(discoveryId: String, pipelineId: String) { (p,d) =>
            TurtleResponse(
                service.getDataSampleService(pipelineId, discoveryId, modelToTtlString(p.dataSample), r.host, ldcpEndpointUri)
            )
        }.getOrElse(NotFound))
    }

    def getDataSample(discoveryId: String, pipelineId: String) = Action.async { r =>
        Future.successful(service.withPipeline(discoveryId: String, pipelineId: String) { (p,_) =>
            TurtleResponse(p.dataSample)
        }.getOrElse(NotFound))
    }

    private def modelToTtlString(model: Model) = {
        val outputStream = new ByteArrayOutputStream()
        RDFDataMgr.write(outputStream, model, Lang.TTL)
        outputStream.toString()
    }

    private def TurtleResponse(model: Model) = {
        Ok(modelToTtlString(model)).as("text/turtle")
    }

    def execute(id: String, pipelineId: String) = Action.async {

        exportPipelineToEtl(id, pipelineId).map { case (etlPipeline, etlPipelineUri) =>

            val prefix = configuration.get[String]("ldcp.etl.hostname")
            val pipelineExecutionUrl = s"$prefix/resources/executions?pipeline=$etlPipelineUri"
            val executionResponse = Http(pipelineExecutionUrl).postForm.asString.body
            val executionIri = Json.parse(executionResponse) \ "iri"

            executionResultDao.insert(ExecutionResult(UUID.randomUUID().toString, id, pipelineId, etlPipeline.resultGraphIri)).map { _ =>
                Ok(Json.obj(
                    "pipelineId" -> pipelineId,
                    "etlPipelineIri" -> etlPipelineUri,
                    "etlExecutionIri" -> executionIri.get.asInstanceOf[JsString].value,
                    "resultGraphIri" -> etlPipeline.resultGraphIri
                ))
            }
        }.flatten.getOrElse(Future.successful(NotFound))
    }

    def createPipeline(id: String, pipelineId: String) = Action.async {
        exportPipelineToEtl(id, pipelineId).map { case (etlPipeline, etlPipelineUri) =>
            Ok(Json.obj(
                "pipelineId" -> pipelineId,
                "etlPipelineIri" -> etlPipelineUri,
                "resultGraphIri" -> etlPipeline.resultGraphIri
            ))
        }.flatten.getOrElse(Future.successful(NotFound))
    }

    private def exportPipelineToEtl(id: String, pipelineId: String) : Option[(EtlPipeline, String)] = {

        val prefix = configuration.get[String]("ldcp.etl.hostname")
        val endpointUri = configuration.get[String]("ldcp.endpointUri")

        service.getEtlPipeline(id, pipelineId, endpointUri).map { etlPipeline =>
            val outputStream = new ByteArrayOutputStream()
            RDFDataMgr.write(outputStream, etlPipeline.dataset, Lang.JSONLD)

            val response = Http(s"$prefix/resources/pipelines").postMulti(
                MultiPart("pipeline", "pipeline.jsonld", "application/ld+json", outputStream.toByteArray)
            ).asString.body

            val resultDataset = DatasetFactory.create()
            RDFDataMgr.read(resultDataset, new StringReader(response), null, Lang.TRIG)
            (etlPipeline, resultDataset.listNames().next())
        }
    }

    def pipeline(id: String, pipelineId: String) = Action {
        val endpointUri = configuration.get[String]("ldcp.endpointUri")
        service.getEtlPipeline(id, pipelineId, endpointUri) match {
            case Some(etlPipeline) => {
                val outputStream = new ByteArrayOutputStream()
                RDFDataMgr.write(outputStream, etlPipeline.dataset, Lang.JSONLD)
                Ok(outputStream.toString())
            }
            case _ => NotFound
        }
    }

    def stop(id: String) = Action {
        service.stop(id)
        Ok(Json.obj())
    }

    def executionStatus(iri: String) = Action {
        Ok(Json.toJson(service.executionStatus(iri)))
    }

}
