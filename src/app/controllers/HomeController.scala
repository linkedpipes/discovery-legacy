package controllers

import javax.inject._

import play.api.libs.json.JsObject
import play.api.mvc._

@Singleton
class HomeController @Inject() extends Controller {

    def index = Action {
        Ok(views.html.index())
    }

    def status = Action {
        Ok(JsObject(Seq()))
    }

    def experiments = Action {
        Ok(views.html.experiments())
    }

}
