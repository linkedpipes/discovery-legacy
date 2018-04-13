package controllers

import java.io._
import java.util.UUID
import java.util.zip.{ZipEntry, ZipOutputStream}
import javax.inject._

import akka.util.ByteString
import controllers.dto.PipelineGrouping
import dao.ExecutionResultDao
import models.ExecutionResult
import org.apache.jena.query.DatasetFactory
import org.apache.jena.riot.{Lang, RDFDataMgr}
import play.Logger
import play.api.Configuration
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import play.api.http.HttpEntity
import play.api.libs.json._
import play.api.mvc._
import services.DiscoveryService
import services.discovery.model.components.DataSourceInstance
import services.discovery.model.{DataSample, Pipeline}
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}
import scalaj.http.{Http, MultiPart}


@Singleton
class DiscoveryController @Inject()(
    service: DiscoveryService,
    configuration: Configuration,
    executionResultDao: ExecutionResultDao,
    protected val dbConfigProvider: DatabaseConfigProvider
)(implicit executionContext: ExecutionContext) extends InjectedController with HasDatabaseConfigProvider[JdbcProfile] {

    val discoveryLogger = Logger.of("discovery")

    def listComponents = Action {
        val templateSourceUri = configuration.get[String]("ldcp.templateSourceUri")
        Ok(
            service.listTemplates(templateSourceUri) match {
                case Some(input) => Json.toJson(input)
                case _ => Json.toJson(Json.obj("error" -> JsString("Error while downloading template data.")))
            }
        )
    }

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

    def requestStats = Action(parse.json(maxLength = 100 * 1024 * 1024)) { request =>
        val discoveries = request.body.as[Seq[JsObject]]
        val pairs = discoveries.map { d =>
          val inputIri = (d \ "inputIri").as[String]
          val discoveryId = (d \ "id").as[String]

            (inputIri, discoveryId)
        }

        Ok(JsObject(Seq(("id", JsString(service.addCsvRequest(pairs).toString)))))
    }

    def getStats(id: String) = Action {
        service.getCsvRequest(UUID.fromString(id)).map { request =>
            val csvFiles = getCsvStats(request)

            val outputByteStream = new ByteArrayOutputStream()
            val zip = new ZipOutputStream(new BufferedOutputStream(outputByteStream))
            csvFiles.foreach { case (name, content) =>
                zip.putNextEntry(new ZipEntry(name))
                zip.write(content.getBytes("UTF-8"))
                zip.closeEntry()
            }
            zip.close()

            Result(
                header = ResponseHeader(200, Map("Content-Disposition" -> "attachment; filename=results.zip")),
                body = HttpEntity.Strict(ByteString(outputByteStream.toByteArray), Some("application/zip"))
            )
        }.getOrElse(NotFound)
    }

    private def getCsvStats(pairs: Seq[(String, String)]) : Seq[(String, String)] = {
        val data = Seq(
            ("1.csv", () => getGlobalCsvStats(pairs)),
            ("2.csv", () => getDataSourceExperimentCsvStats(pairs)),
            ("3.csv", () => getApplicationExperimentCsvStats(pairs)),
            ("4.csv", () => getDataSourceApplicationExperimentCsvStats(pairs))
        ) ++ pairs.map(p => (s"${p._2}.csv", () => getDetailedCsv(p._2)))

        data.par.map{ case (n, f) => {
            (n, f())
        }}.seq
    }

    private def toCsv(input: Seq[Any]) : String = {
        input.map(i => "\"" + i.toString.replace("\"","\"\"") + "\"").mkString(",")
    }

    private def getDataSourceExperimentCsvStats(pairs: Seq[(String, String)]) : String = {

        val heading = toCsv(Seq(
            "Discovery ID",
            "Experiment URI",
            "Datasource URI",
            "Datasource template label",
            "Extractor group count",
            "App count",
            "Pipeline count"
        ))

        val lines = pairs.map { case (inputIri, id) =>
            service.withDiscovery(id) { discovery =>
                val dataSources = discovery.input.dataSets.map(ds => ds.dataSourceInstance)
                dataSources.map { d =>
                    toCsv(Seq(
                        id,
                        inputIri,
                        d.iri,
                        d.label,
                        service.getPipelinesOfDiscovery(id).map(PipelineGrouping.create).map { g =>
                            g.applicationGroups.map(_.dataSourceGroups.filter(_.dataSourceInstances.contains(d)).map(_.extractorGroups).size).sum
                        }.get,
                        service.getPipelinesOfDiscovery(id).map(PipelineGrouping.create).map { g =>
                            g.applicationGroups.count(_.dataSourceGroups.exists(_.dataSourceInstances.contains(d)))
                        }.get,
                        service.getPipelinesOfDiscovery(id).map(pipelines => pipelines.count(p => p._2.components.exists(c => c.componentInstance == d))).get
                    ))
                }.mkString("\n")
            }.get
        }.mkString("\n")

        Seq(heading, lines).mkString("\n")

    }

    private def getApplicationExperimentCsvStats(pairs: Seq[(String, String)]) : String = {
        val heading = toCsv(Seq(
            "Discovery ID",
            "Experiment URI",
            "Application URI",
            "Application template label",
            "Extractor group count",
            "Datasource count",
            "Pipeline count"
        ))

        val lines = pairs.map { case (inputIri, id) =>
            service.withDiscovery(id) { d =>
                d.input.applications.map { a =>
                    toCsv(Seq(
                        id,
                        inputIri,
                        a.iri,
                        a.label,
                        service.getPipelinesOfDiscovery(id).map(PipelineGrouping.create).map { g =>
                            g.applicationGroups.filter(_.applicationInstance == a).map(_.dataSourceGroups.map(_.extractorGroups).size).sum
                        }.get,
                        service.getPipelinesOfDiscovery(id).map(PipelineGrouping.create).map { g =>
                            g.applicationGroups.filter(_.applicationInstance == a).flatMap(_.dataSourceGroups.map(_.dataSourceInstances)).distinct.size
                        }.get,
                        service.getPipelinesOfDiscovery(id).map(pipelines => pipelines.count(p => p._2.components.exists(c => c.componentInstance == a))).get
                    ))
                }.mkString("\n")
            }.get
        }.mkString("\n")

        Seq(heading, lines).mkString("\n")
    }

    private def getDataSourceApplicationExperimentCsvStats(pairs: Seq[(String, String)]) : String = {

        val heading = toCsv(Seq(
            "Discovery ID",
            "Experiment URI",
            "Datasource URI",
            "Application URI",
            "DataSource template label",
            "Application template label",
            "Group count?",
            "Pipeline count"
        ))

        val lines = pairs.map { case (inputIri, id) =>
            service.withDiscovery(id) { discovery =>
                discovery.input.applications.flatMap { a =>
                    val dataSources = discovery.input.dataSets.map(ds => ds.dataSourceInstance)
                    dataSources.map { d =>
                        toCsv(Seq(
                            id,
                            inputIri,
                            d.iri,
                            a.iri,
                            d.label,
                            a.label,
                            service.getPipelinesOfDiscovery(id).map(PipelineGrouping.create).map { g =>
                                g.applicationGroups.filter(ag => ag.applicationInstance == a).flatMap(ag => ag.dataSourceGroups).filter(_.dataSourceInstances == d).map(_.extractorGroups.map(_.dataSampleGroups.size).sum).sum
                            }.get,
                            service.getPipelinesOfDiscovery(id).map(pipelines => pipelines.count { p =>
                                p._2.components.exists(c => c.componentInstance == a) && p._2.components.exists(c => c.componentInstance == d)
                            }).get
                        ))
                    }
                }.mkString("\n")
            }.get
        }.mkString("\n")

        Seq(heading, lines).mkString("\n")
    }

    private def getGlobalCsvStats(pairs: Seq[(String, String)]) : String = {

        val heading = toCsv(Seq(
            "Discovery ID",
            "Experiment URI",
            "Application group count",
            "Datasource group count",
            "Extractor group count",
            "Data sample group count",
            "Discovery duration",
            "Application count",
            "Data source count",
            "Transformer count"
        ))

        val lines = pairs.map { case (inputIri, id) =>
            service.withDiscovery(id) { d =>
                service.getPipelinesOfDiscovery(id).map { pipelineMap => PipelineGrouping.create(pipelineMap) }.map { g =>
                    toCsv(Seq(
                        id,
                        inputIri,
                        g.applicationGroups.size,
                        g.applicationGroups.map(ag => ag.dataSourceGroups.size).sum,
                        g.applicationGroups.map(ag => ag.dataSourceGroups.map(ds => ds.extractorGroups.size).sum).sum,
                        g.applicationGroups.map(ag => ag.dataSourceGroups.map(ds => ds.extractorGroups.map(eg => eg.dataSampleGroups.size).sum).sum).sum,
                        g.pipelines.size,
                        d.duration,
                        d.input.applications.size,
                        d.input.dataSets.size,
                        d.input.processors.size
                    ))
                }.get
            }.get
        }.mkString("\n")

        Seq(heading, lines).mkString("\n")
    }

    def csv(id: String) = Action {
        Ok(getDetailedCsv(id))
    }

    private def getDetailedCsv(id: String) = {
        val maybeGrouping = service.getPipelinesOfDiscovery(id).map { pipelineMap => PipelineGrouping.create(pipelineMap) }
        val lines = maybeGrouping.map { grouping =>
            grouping.applicationGroups.map { applicationGroup =>
                applicationGroup.dataSourceGroups.map { dataSourceGroup =>
                    dataSourceGroup.extractorGroups.map { extractorGroup =>
                        extractorGroup.dataSampleGroups.sortBy(g => g.minimalIteration).map { dataSampleGroup =>
                            val pipelines = dataSampleGroup.pipelines.toSeq.sortBy(p => p._2.lastComponent.discoveryIteration)

                            pipelines.map { p =>
                                val dataSourcesString = p._2.typedDatasources.map(_.label).mkString(",")
                                val extractorsString = p._2.typedExtractors.map(_.getClass.getSimpleName).mkString(",")
                                val transformersString = p._2.typedProcessors.map(_.label).mkString(",")
                                val transformersCount = p._2.typedProcessors.size
                                val app = p._2.typedApplications.map(_.label).mkString(",")
                                val iterationNumber = p._2.lastComponent.discoveryIteration

                                toCsv(Seq(
                                    id,
                                    dataSourcesString,
                                    transformersCount,
                                    extractorsString,
                                    transformersString,
                                    app,
                                    iterationNumber,
                                    s"/discovery/$id/execute/${p._1}"
                                ))
                            }.mkString("\n")
                        }.mkString("\n")
                    }.mkString("\n")
                }.mkString("\n")
            }.mkString("\n")
        }.mkString("\n")

        val header = toCsv(Seq(
            "discoveryId",
            "appGroup",
            "dataSourcesGroup",
            "extractorsGroup",
            "dataSampleGroup",
            "dataSources",
            "transformerCount",
            "extractors",
            "transformers",
            "app",
            "iterationNumber"
        ))

        Seq(header, lines).mkString("\n")
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
                val model = service.getService(executionResult, r.host, configuration.get[String]("ldcp.endpointUri"))
                val outputStream = new ByteArrayOutputStream()
                RDFDataMgr.write(outputStream, model, Lang.TTL)
                Ok(outputStream.toString())
            }
            case _ => NotFound
        }
    }

    def getDataSampleSparqlService(discoveryId: String, pipelineId: String) = Action.async { r =>
        Future.successful(service.withPipeline(discoveryId: String, pipelineId: String) { (p,d) =>
            val model = service.getDataSampleService(pipelineId, discoveryId, p.dataSample, r.host, configuration.get[String]("ldcp.endpointUri"))
            val outputStream = new ByteArrayOutputStream()
            RDFDataMgr.write(outputStream, model, Lang.TTL)
            Ok(outputStream.toString())
        }.getOrElse(NotFound))
    }

    def execute(id: String, pipelineId: String) = Action.async {

        val prefix = configuration.get[String]("ldcp.etl.hostname")
        val endpointUri = configuration.get[String]("ldcp.endpointUri")

        service.getEtlPipeline(id, pipelineId, endpointUri) match {
            case Some(etlPipeline) => {
                val outputStream = new ByteArrayOutputStream()
                RDFDataMgr.write(outputStream, etlPipeline.dataset, Lang.JSONLD)

                val response = Http(s"$prefix/resources/pipelines").postMulti(
                    MultiPart("pipeline", "pipeline.jsonld", "application/ld+json", outputStream.toByteArray)
                ).asString.body

                val resultDataset = DatasetFactory.create()
                RDFDataMgr.read(resultDataset, new StringReader(response), null, Lang.TRIG)
                val pipelineUri = resultDataset.listNames().next()

                val pipelineExecutionUrl = s"$prefix/resources/executions?pipeline=$pipelineUri"
                val executionResponse = Http(pipelineExecutionUrl).postForm.asString.body
                val executionIri = Json.parse(executionResponse) \ "iri"

                executionResultDao.insert(ExecutionResult(UUID.randomUUID().toString, id, pipelineId, etlPipeline.resultGraphIri)).map { _ =>
                    Ok(Json.obj(
                        "pipelineId" -> pipelineId,
                        "etlPipelineIri" -> pipelineUri,
                        "etlExecutionIri" -> executionIri.get.asInstanceOf[JsString].value,
                        "resultGraphIri" -> etlPipeline.resultGraphIri
                    ))
                }
            }
            case _ => Future.successful(NotFound)
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
