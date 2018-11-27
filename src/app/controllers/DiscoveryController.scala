package controllers

import java.io._
import java.util.UUID

import javax.inject._
import controllers.dto.{PipelineGrouping, PipelineKey, SparqlEndpointDefinition, SparqlEndpointGraph}
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
import services.{DiscoveryService, RdfUtils}
import services.discovery.model.components.DataSourceInstance
import services.discovery.model.{DataSample, GuidGenerator, Pipeline}
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}
import scalaj.http.{Http, MultiPart}
import services.discovery.Discovery
import services.discovery.model.etl.EtlPipeline


@Singleton
class DiscoveryController @Inject()(
    service: DiscoveryService,
    configuration: Configuration,
    executionResultDao: ExecutionResultDao,
    protected val dbConfigProvider: DatabaseConfigProvider
)(implicit executionContext: ExecutionContext) extends InjectedController with HasDatabaseConfigProvider[JdbcProfile] {

    private val discoveryLogger = Logger.of("discovery")
    private val ldcpEndpoint = configuration.get[SparqlEndpointDefinition]("ldcp.sparqlEndpoint")
    private val templateSourceUri = configuration.get[String]("ldcp.templateSourceUri")

    def listComponents = Action {
        Ok(
            service.listTemplates(templateSourceUri) match {
                case Right(input) => Json.toJson(input)
                case Left(e) => businessError(s"Error while downloading template data: ${e.getMessage}")
            }
        )
    }

    def start: Action[JsValue] = Action(parse.json) { request =>
        val uris = request.body.as[Seq[String]]
        discoveryStarted(service.start(uris))
    }

    def startFromInputIri(iri: String) = Action {
        discoveryStarted(service.startFromInputIri(iri))
    }

    def startExperimentFromIri(experiemntIri: String) = Action {
        service.startExperimentFromIri(experiemntIri)
        Ok("running")
    }

    def getDiscoveryInputIrisFromExperimentIri(iri: String) = Action {
        inputsExtracted(service.getDiscoveryInputIrisFromExperimentIri(iri))
    }

    def starFromInput = Action { request: Request[AnyContent] =>
        val body: AnyContent = request.body
        discoveryStarted(service.startFromInput(body.asText.getOrElse("")))
    }

    def getDiscoveryInputIrisFromExperiment = Action { request: Request[AnyContent] =>
        val body: AnyContent = request.body
        inputsExtracted(service.getDiscoveryInputIrisFromExperiment(body.asText.getOrElse("")))
    }

    def status(id: String) = Action {
        Ok(Json.toJson(service.getStatus(id)))
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

    def getSparqlService(discoveryId: String, pipelineId: String) = Action.async { r =>
        executionResultDao.findByPipelineKey(PipelineKey(discoveryId, pipelineId)).map {
            case Some(executionResult) => {
                RdfAsTurtle(
                    service.getService(ldcpEndpoint, executionResult, r.host)
                )
            }
            case _ => NotFound
        }
    }

    def getDataSampleSparqlService(discoveryId: String, pipelineId: String) = Action.async { r =>
        Future.successful(service.withPipeline(PipelineKey(discoveryId, pipelineId)) { (p,_) =>
            val graph = service.storeDataSample(p.dataSample, ldcpEndpoint, discoveryId, pipelineId)
            RdfAsTurtle(service.getDataSampleService(r.host, graph, discoveryId, pipelineId))
        }.getOrElse(NotFound))
    }

    def getDataSample(discoveryId: String, pipelineId: String) = Action.async { r =>
        Future.successful(service.withPipeline(PipelineKey(discoveryId, pipelineId)) { (p,_) =>
            RdfAsTurtle(p.dataSample)
        }.getOrElse(NotFound))
    }

    private def sampleEquals(ds1: DataSample, ds2: DataSample): Boolean = {
        val uuid = UUID.randomUUID()
        val d = ds1.getModel(uuid, 0).difference(ds2.getModel(uuid, 0))
        d.isEmpty
    }

    private def minIteration(pipelines: Seq[Pipeline]): Int = {
        pipelines.map(p => p.lastComponent.discoveryIteration).min
    }

    private def businessError(errorMessage: String) = Json.toJson(Json.obj("error" -> Json.obj("message" -> JsString(errorMessage))))

    private def RdfAsTurtle(model: Model) = {
        Ok(RdfUtils.modelToTtl(model)).as("text/turtle")
    }

    private def discoveryStarted(discovery: Discovery) = {
        Ok(Json.obj("id" -> Json.toJson(discovery.id)))
    }

    private def inputsExtracted(inputIris: Seq[String]) = {
        Ok(Json.obj("inputIris" -> inputIris.map(ii => Json.toJson(ii))))
    }

    def execute(id: String, pipelineId: String) = Action.async {

        exportPipelineToEtl(PipelineKey(id, pipelineId)).map { case (etlPipeline, etlPipelineUri) =>

            val prefix = configuration.get[String]("ldcp.etl.hostname")
            val pipelineExecutionUrl = s"$prefix/resources/executions?pipeline=$etlPipelineUri"
            val executionResponse = Http(pipelineExecutionUrl).postForm.asString.body
            val executionIri = Json.parse(executionResponse) \ "iri"

            executionResultDao.insert(ExecutionResult(UUID.randomUUID().toString, id, pipelineId, etlPipeline.resultGraph.iri)).map { _ =>
                Ok(Json.obj(
                    "pipelineId" -> pipelineId,
                    "etlPipelineIri" -> etlPipelineUri,
                    "etlExecutionIri" -> executionIri.get.asInstanceOf[JsString].value,
                    "resultGraphIri" -> etlPipeline.resultGraph.iri
                ))
            }
        }.getOrElse(Future.successful(NotFound))
    }

    def createPipeline(id: String, pipelineId: String) = Action.async {
        Future.successful(
            exportPipelineToEtl(PipelineKey(id, pipelineId)).map { case (etlPipeline, etlPipelineUri) =>
                Ok(Json.obj(
                    "pipelineId" -> pipelineId,
                    "etlPipelineIri" -> etlPipelineUri,
                    "resultGraphIri" -> etlPipeline.resultGraph.iri
                ))
            }.getOrElse(NotFound)
        )
    }

    def exportPipeline(id: String, pipelineId: String) =  Action(parse.json) { request =>
        val body = request.body.as[Map[String, String]]
        val sdIri = body("sdIri")
        exportPipelineToEtl(PipelineKey(id, pipelineId), sdIri).map { case (etlPipeline, etlPipelineUri) =>
            Ok(Json.obj(
                "pipelineId" -> pipelineId,
                "etlPipelineIri" -> etlPipelineUri,
                "sdIri" -> sdIri
            ))
        }.getOrElse(NotFound)
    }

    private def exportPipelineToEtl(pipelineKey: PipelineKey) : Option[(EtlPipeline, String)] = {
        val graph = SparqlEndpointGraph(ldcpEndpoint, GuidGenerator.nextIri)
        etlExport(pipelineKey, graph)
    }

    private def exportPipelineToEtl(pipelineKey: PipelineKey, sdIri: String) : Option[(EtlPipeline, String)] = {
        RdfUtils.readServiceDescription(sdIri)(discoveryLogger) {
            case Right(sparqlEndpointGraph) => {
                etlExport(pipelineKey, sparqlEndpointGraph)
            }
            case _ => None
        }
    }

    private def etlExport(pipelineKey: PipelineKey, endpointGraph: SparqlEndpointGraph) = {
        val prefix = configuration.get[String]("ldcp.etl.uri")

        service.getEtlPipeline(pipelineKey, endpointGraph).map { etlPipeline =>
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
        val graph = SparqlEndpointGraph(ldcpEndpoint, GuidGenerator.nextIri)
        service.getEtlPipeline(PipelineKey(id, pipelineId), graph) match {
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
