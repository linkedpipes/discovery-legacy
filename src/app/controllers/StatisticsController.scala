package controllers

import java.util.UUID

import akka.util.ByteString
import controllers.dto.{CsvFile, CsvRequestData}
import javax.inject._
import play.api.Configuration
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import play.api.http.HttpEntity
import play.api.libs.json._
import play.api.mvc._
import services.{DiscoveryService, StatisticsService}
import slick.jdbc.JdbcProfile

import scala.concurrent.ExecutionContext


@Singleton
class StatisticsController @Inject()(
    discoveryService: DiscoveryService,
    statisticsService: StatisticsService,
    configuration: Configuration,
    protected val dbConfigProvider: DatabaseConfigProvider
)(implicit executionContext: ExecutionContext) extends InjectedController with HasDatabaseConfigProvider[JdbcProfile] {

    def request = Action(parse.json(maxLength = 100 * 1024 * 1024)) { request =>
        val discoveries = request.body.as[Seq[JsObject]]
        val indexes = discoveries.map { d =>
            val inputIri = (d \ "inputIri").as[String]
            val discoveryId = (d \ "id").as[String]

            CsvRequestData(inputIri, discoveryId)
        }

        val requestId = statisticsService.addCsvRequest(indexes)
        Ok(JsObject(Seq(("id", JsString(requestId.toString)))))
    }

    def get(id: String) = Action {
        statisticsService.getCsvRequest(UUID.fromString(id)).map { request =>

            val outputByteStream = statisticsService.getZip(request, discoveryService)

            Result(
                header = ResponseHeader(200, Map("Content-Disposition" -> "attachment; filename=results.zip")),
                body = HttpEntity.Strict(ByteString(outputByteStream.toByteArray), Some("application/zip"))
            )
        }.getOrElse(NotFound)
    }

    def csv(id: String) = Action {
        Ok(CsvFile("details.csv", statisticsService.getDetailedCsv(id, discoveryService)).content)
    }

}
