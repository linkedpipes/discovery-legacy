package services.discovery.model.components

import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Writes}
import services.discovery.model.Port

trait ExtractorInstance extends AnalyzerInstance

trait SparqlConstructExtractorInstance extends ExtractorInstance with SparqlAnalyzerInstance {
    def port: Port
}

object ExtractorInstance {
    implicit val writes : Writes[ExtractorInstance] = (
        (JsPath \ "portCount").write[Int] and
            (JsPath \ "name").write[String]
        )(unlift(ExtractorInstance.destruct))

    def destruct(i: ExtractorInstance) : Option[(Int, String)] = {
        Some((i.getInputPorts.size, i.getClass.getSimpleName))
    }
}