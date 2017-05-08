package services.discovery.model.components

import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Writes}

trait ApplicationInstance extends ComponentInstanceWithInputs

object ApplicationInstance {
    implicit val writes : Writes[ApplicationInstance] = (
        (JsPath \ "portCount").write[Int] and
            (JsPath \ "name").write[String]
        )(unlift(ApplicationInstance.destruct))

    def destruct(i: ApplicationInstance) : Option[(Int, String)] = {
        Some((i.getInputPorts.size, i.getClass.getSimpleName))
    }
}