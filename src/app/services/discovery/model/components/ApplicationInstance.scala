package services.discovery.model.components

import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Writes}

trait ApplicationInstance extends ComponentInstanceWithInputs

object ApplicationInstance {
    implicit val writes : Writes[ApplicationInstance] = (
        (JsPath \ "portCount").write[Int] and
            (JsPath \ "uri").write[String] and
            (JsPath \ "label").write[String]
        )(unlift(ApplicationInstance.destruct))

    def destruct(i: ApplicationInstance) : Option[(Int, String, String)] = {
        Some((i.getInputPorts.size, i.uri, i.label))
    }
}