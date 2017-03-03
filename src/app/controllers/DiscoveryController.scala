package controllers

import java.io._
import java.util.UUID
import javax.inject._

import org.apache.jena.query.DatasetFactory
import org.apache.jena.rdf.model.{Model, ModelFactory, Resource}
import org.apache.jena.riot.{Lang, RDFDataMgr, RiotException}
import org.apache.jena.vocabulary.{RDF, RDFS}
import play.Logger
import play.api.libs.json.{JsNumber, JsString, Json}
import play.api.libs.ws.WSClient
import play.api.mvc.{Action, Controller}
import services.DiscoveryService
import services.discovery.model.components.DataSourceInstance
import services.discovery.model.{DataSample, DiscoveryInput, Pipeline}

import scala.collection.JavaConverters._
import scala.collection.mutable
import scalaj.http.{Http, MultiPart}

@Singleton
class DiscoveryController @Inject()(service: DiscoveryService, ws: WSClient) extends Controller {

    val discoveryLogger = Logger.of("discovery")

    def start(uri: String) = Action {
        fromUri(uri) {
            case Right(model) => {
                val experiments = model.listSubjectsWithProperty(RDF.`type`, RDF.Bag).asScala
                val templatesByExperiment = experiments.map { e => getTemplates(model, e) }.toSeq
                val errors = templatesByExperiment.flatMap(_._2)
                val templates = templatesByExperiment.map(_._1)
                if (errors.nonEmpty) {
                    BadRequest(s"Referenced data contains some error: ${errors.map(_.getMessage).mkString(";")}.")
                } else {
                    val ids = templates.map(t => runExperiment(t))
                    Ok(Json.toJson(ids))
                }
            }
            case Left(e) => BadRequest(s"Referenced data contains some error: $uri. ${e.getMessage}.")
        }
    }

    def status(id: String) = Action {
        val result = service.getStatus(id)
        Ok(Json.toJson(result))
    }

    def list(id: String) = Action {
        val maybePipelines = service.getPipelines(id)
        Ok(Json.obj(
            "pipelines" -> maybePipelines.map { pipelines => pipelines.map { p =>
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

    def csv(id: String) = Action {
        val maybePipelines = service.getPipelines(id)
        val string = maybePipelines.map { pipelineMap => {

            val appGroups = pipelineMap.groupBy(p => p._2.typedVisualizers.head)

            var ag = 0
            appGroups.map { appGroup =>
                ag += 1
                val dataSourceGroups = appGroup._2.groupBy(p => p._2.typedDatasources.toSet)
                var dg = 0
                dataSourceGroups.map { dataSourceGroup =>
                    dg += 1
                    val extractorGroups = dataSourceGroup._2.groupBy(p => p._2.typedExtractors.toSet)
                    var eg = 0
                    extractorGroups.map { extractorGroup =>
                        eg += 1
                        var groupedPipelines = extractorGroup._2.toIndexedSeq.sortBy(p => p._2.lastComponent.discoveryIteration)
                        var i = 1
                        val groups = new mutable.HashMap[Int, Seq[(UUID, Pipeline)]]

                        while (groupedPipelines.nonEmpty) {
                            val groupedPipeline = groupedPipelines.head
                            val same = for {
                                pCandid <- groupedPipelines.drop(1) if sampleEquals(groupedPipeline._2.lastOutputDataSample, pCandid._2.lastOutputDataSample)
                            } yield pCandid

                            val group = Seq(groupedPipeline) ++ same
                            groups.put(i, group)
                            i += 1
                            groupedPipelines = groupedPipelines.filter(pip => !group.contains(pip))
                        }

                        groups.toIndexedSeq.sortBy(pg => minIteration(pg._2.map(_._2))).map { case (idx, pGroup) =>
                            val pipelines = pGroup.sortBy(p => p._2.lastComponent.discoveryIteration)

                            pipelines.map { p =>
                                val datasourcesString = p._2.typedDatasources.map(_.label).mkString(",")
                                val extractorsString = p._2.typedExtractors.map(_.getClass.getSimpleName).mkString(",")
                                val transformersString = p._2.typedProcessors.map(_.getClass.getSimpleName).mkString(",")
                                val transformersCount = p._2.typedProcessors.size
                                val app = p._2.typedVisualizers.map(_.getClass.getSimpleName).mkString(",")
                                val iterationNumber = p._2.lastComponent.discoveryIteration

                                s"$ag;$dg;$eg;$idx;$datasourcesString;$transformersCount;$extractorsString;$transformersString;$app;$iterationNumber;/discovery/$id/execute/${p._1}"
                            }.mkString("\n")
                        }.mkString("\n")
                    }.mkString("\n")
                }.mkString("\n")
            }.mkString("\n")
        }
        }.getOrElse("")

        val header = s"appGroup;dataSourcesGroup;extractorsGroup;dataSampleGroup;dataSources;transformerCount;extractors;transformers;app;iterationNumber"

        Ok(s"$header\n$string")
    }

    def sampleEquals(ds1: DataSample, ds2: DataSample): Boolean = {
        val uuid = UUID.randomUUID()
        val d = ds1.getModel(uuid, 0).difference(ds2.getModel(uuid, 0))
        d.isEmpty
    }

    def minIteration(pipelines: Seq[Pipeline]): Int = {
        pipelines.map(p => p.lastComponent.discoveryIteration).min
    }

    def execute(id: String, pipelineId: String) = Action {
        val etlPipeline = service.getEtlPipeline(id, pipelineId)
        val prefix = "http://xrg12.ms.mff.cuni.cz:8090"

        etlPipeline.map { ep =>
            val outputStream = new ByteArrayOutputStream()
            RDFDataMgr.write(outputStream, ep.dataset, Lang.JSONLD)

            val pipelineCreationUrl = s"$prefix/resources/pipelines"
            val response = Http(pipelineCreationUrl).postMulti(MultiPart("pipeline", "pipeline.jsonld", "application/ld+json", outputStream.toByteArray)).asString.body

            val resultDataset = DatasetFactory.create()
            RDFDataMgr.read(resultDataset, new StringReader(response), null, Lang.TRIG)
            val pipelineUri = resultDataset.listNames().next()

            val pipelineExecutionUrl = s"$prefix/resources/executions?pipeline=$pipelineUri"
            val executionResponse = Http(pipelineExecutionUrl).postForm.asString.body
            val executionIri = Json.parse(executionResponse) \ "iri"

            Ok(Json.obj(
                "pipelineId" -> pipelineId,
                "etlPipelineIri" -> pipelineUri,
                "etlExecutionIri" -> executionIri.get.asInstanceOf[JsString].value,
                "resultGraphIri" -> ep.resultGraphIri
            ))
        }.getOrElse(NotFound)
    }

    def pipeline(id: String, pipelineId: String) = Action {
        val etlPipeline = service.getEtlPipeline(id, pipelineId)
        etlPipeline.map { ep =>
            val outputStream = new ByteArrayOutputStream()
            RDFDataMgr.write(outputStream, ep.dataset, Lang.JSONLD)
            Ok(outputStream.toString())
        }.getOrElse(NotFound)
    }

    def stop(id: String) = Action {
        service.stop(id)
        Ok(Json.obj())
    }

    private def fromUri[R](uri: String)(fn: Either[Throwable, Model] => R): R = {
        discoveryLogger.debug(s"Downloading data from $uri.")
        val result = try {
            val model = ModelFactory.createDefaultModel()
            model.read(uri)
            Right(model)
        } catch {
            case e: RiotException => Left(new Exception(s"The data at $uri caused the following error: ${e.getMessage}."))
        }
        fn(result)
    }

    private def getTemplates(model: Model, bag: Resource): (Seq[Model], Seq[Throwable]) = {
        val templates = model.listObjectsOfProperty(bag, RDFS.member).asScala
        val (lefts, rights) = templates.map(t => fromUri(t.asResource().getURI) { e => e }).partition(_.isLeft)
        (rights.map(_.right.get).toSeq, lefts.map(_.left.get).toSeq)
    }

    private def runExperiment(templates: Seq[Model]): UUID = {
        val discoveryInput = DiscoveryInput(templates)
        service.start(discoveryInput)
    }

}
