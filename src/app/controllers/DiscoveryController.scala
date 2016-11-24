package controllers

import java.io.ByteArrayOutputStream
import javax.inject._

import controllers.dto.DiscoverySettings
import org.apache.jena.riot.{Lang, RDFDataMgr}
import play.Logger
import play.api.libs.json.{JsError, JsNumber, Json}
import play.api.libs.ws.WSClient
import play.api.mvc.{Action, BodyParsers, Controller}
import services.DiscoveryService
import services.discovery.model.components.DataSourceInstance
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
                if (settings.sparqlEndpoints.size == 0) {
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

    def upload(id: String) = Action {
        val url = "http://xrg12.ms.mff.cuni.cz:8090/resources/pipelines?pipeline="
        val maybePipelines = service.getPipelines(id)
        maybePipelines.map { p =>
            p.map { case (uuid, pipeline) =>
                val request = ws.url(url + s"http://demo.visualization.linkedpipes.com/discovery/$id/pipelines/${uuid.toString}")
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
