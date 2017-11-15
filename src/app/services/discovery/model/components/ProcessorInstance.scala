package services.discovery.model.components

import play.api.libs.functional.syntax._
import play.api.libs.functional.syntax.unlift
import play.api.libs.json.{JsPath, Writes}

trait ProcessorInstance extends ComponentInstanceWithInputs with ComponentInstanceWithOutput {

}

object ProcessorInstance {
    implicit val writes : Writes[ProcessorInstance] = (
        (JsPath \ "portCount").write[Int] and
            (JsPath \ "name").write[String] and
            (JsPath \ "iri").write[String] and
            (JsPath \ "label").write[String]
        )(unlift(ProcessorInstance.destruct))

    def destruct(i: ProcessorInstance) : Option[(Int, String, String, String)] = {
        Some((i.getInputPorts.size, i.getClass.getSimpleName, i.iri, i.label))
    }
}