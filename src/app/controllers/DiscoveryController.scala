package controllers

import java.io._
import java.util.UUID
import javax.inject._

import controllers.dto.DiscoverySettings
import org.apache.jena.riot.{Lang, RDFDataMgr}
import play.Logger
import play.api.libs.json.{JsError, JsNumber, Json}
import play.api.libs.ws.WSClient
import play.api.mvc.{Action, BodyParsers, Controller}
import services.DiscoveryService
import services.discovery.model.{DataSample, Pipeline}
import services.discovery.model.components.{DataSourceInstance, ExtractorInstance, TransformerInstance, ApplicationInstance}

import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class DiscoveryController @Inject()(service: DiscoveryService, ws: WSClient) extends Controller {

    def start = Action(BodyParsers.parse.json) { request =>
        Logger.debug(s"[${request.id}] A discovery was requested: ${request.body}.")
        val settings = request.body.validate[DiscoverySettings]
        settings.fold(
            errors => {
                Logger.debug(s"[${request.id}] Rejected discovery due to JSON errors: ${errors.toString()}")
                BadRequest(Json.obj("error" -> JsError.toJson(errors)))
            },
            settings => {
                if (settings.sparqlEndpoints.isEmpty) {
                    Logger.debug(s"[${request.id}] Rejected discovery. Neither dumps or SPARQL endpoints were specified.")
                    BadRequest(Json.obj("error" -> "Either dumps or sparql endpoints have to be specified."))
                } else {
                    val id = service.start(settings)
                    Logger.debug(s"[${request.id}] Running discovery $id with settings: ${settings.toString}.")
                    Ok(Json.obj("id" -> Json.toJson(id)))
                }
            }
        )
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

                val pipelines = pipelineMap.map { case (_, p) => p }
                val appGroups = pipelines.groupBy(p => p.typedVisualizers.head)

                var ag = 0
                appGroups.map { appGroup =>
                    ag += 1
                    val dataSourceGroups = appGroup._2.groupBy(p => p.typedDatasources.toSet)
                    var dg = 0
                    dataSourceGroups.map { dataSourceGroup =>
                        dg += 1
                        val extractorGroups = dataSourceGroup._2.groupBy(p => p.typedExtractors.toSet)
                        var eg = 0
                        extractorGroups.map { extractorGroup =>
                            eg += 1
                            var groupedPipelines = extractorGroup._2.toIndexedSeq.sortBy(p => p.lastComponent.discoveryIteration)
                            var i = 1
                            val groups = new mutable.HashMap[Int, Seq[Pipeline]]

                            while(groupedPipelines.nonEmpty)
                            {
                                val groupedPipeline = groupedPipelines.head
                                val same = for {
                                    pCandid <- groupedPipelines.drop(1) if sampleEquals(groupedPipeline.lastOutputDataSample, pCandid.lastOutputDataSample)
                                } yield pCandid

                                val group = Seq(groupedPipeline) ++ same
                                groups.put(i, group)
                                i += 1
                                groupedPipelines = groupedPipelines.filter(pip => !group.contains(pip))
                            }

                            groups.toIndexedSeq.sortBy(pg => minIteration(pg._2)).map { case (idx, pGroup) =>
                                val pipelines = pGroup.sortBy(p => p.lastComponent.discoveryIteration)

                                pipelines.map { p =>
                                    val datasourcesString = p.typedDatasources.map(_.label).mkString(",")
                                    val extractorsString = p.typedExtractors.map(_.getClass.getSimpleName).mkString(",")
                                    val transformersString = p.typedTransformers.map(_.getClass.getSimpleName).mkString(",")
                                    val transformersCount = p.typedTransformers.size
                                    val app = p.typedVisualizers.map(_.getClass.getSimpleName).mkString(",")
                                    val iterationNumber = p.lastComponent.discoveryIteration

                                    s"$ag;$dg;$eg;$idx;$datasourcesString;$transformersCount;$extractorsString;$transformersString;$app;$iterationNumber"
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

    def minIteration(pipelines: Seq[Pipeline]) : Int = {
        pipelines.map(p => p.lastComponent.discoveryIteration).min
    }

    def upload(id: String) = Action {
        val url = "http://xrg12.ms.mff.cuni.cz:8090/resources/pipelines?pipeline="
        val maybePipelines = service.getPipelines(id)
        maybePipelines.foreach { p =>
            p.foreach { case (uuid, pipeline) =>
                val request = ws.url(url + s"http://demo.visualization.linkedpipes.com:8080/discovery/$id/pipelines/${uuid.toString}")
                request.get().foreach { r => println(r.body) }
            }
        }
        Ok(Json.obj())
    }

    def pipeline(id: String, pipelineId: String) = Action {
        val data = service.getEtlPipeline(id, pipelineId)
        data.map { datasets =>
            val outputStream = new ByteArrayOutputStream()
            datasets.foreach(d =>
                RDFDataMgr.write(outputStream, d, Lang.JSONLD)
            )
            Ok(outputStream.toString())
        }.getOrElse(NotFound)
    }

    def stop(id: String) = Action {
        service.stop(id)
        Ok(Json.obj())
    }

}
