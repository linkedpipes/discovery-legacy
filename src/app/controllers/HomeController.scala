package controllers

import java.util.UUID
import javax.inject._

import dao.DiscoveryResultDao
import models.DiscoveryResult
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import play.api.libs.json.{JsObject, JsString}
import play.api.mvc._
import slick.jdbc.JdbcProfile

import scala.concurrent.ExecutionContext

@Singleton
class HomeController @Inject()(
    discoveryResultDao: DiscoveryResultDao,
    protected val dbConfigProvider: DatabaseConfigProvider
)(implicit executionContext: ExecutionContext) extends InjectedController with HasDatabaseConfigProvider[JdbcProfile] {

    def index = Action {
        Ok(views.html.index())
    }

    def status = Action {
        Ok(JsObject(Seq()))
    }

    def experiments = Action {
        Ok(views.html.experiments())
    }

    def persist = Action(parse.json) { request =>
        val data = request.body
        val id = (data \ "discovery" \ "id").get.validate[String].get
        discoveryResultDao.insert(DiscoveryResult(id, data.toString()))
        Ok(JsObject(Seq("id" -> JsString(id))))
    }

    def result(id: String) = Action.async {
        discoveryResultDao.findById(id).map {
            case Some(result) => Ok(result.data)
            case _ => NotFound
        }
    }

}
