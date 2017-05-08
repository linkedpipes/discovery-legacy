package services.discovery.model.components

import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Writes}

trait DataSourceInstance extends ComponentInstanceWithOutput {

  def isLarge: Boolean

  def isLinkset: Boolean

  def label: String

}

trait SparqlEndpointInstance extends DataSourceInstance {

  def url: String

  def defaultGraphIris: Seq[String]

}


object DataSourceInstance {
  implicit val writes : Writes[DataSourceInstance] = (
      (JsPath \ "isLarge").write[Boolean] and
          (JsPath \ "isLinkset").write[Boolean] and
          (JsPath \ "label").write[String]
      )(unlift(DataSourceInstance.destruct))

  def destruct(i: DataSourceInstance) : Option[(Boolean, Boolean, String)] = {
    Some((i.isLarge, i.isLinkset, i.label))
  }
}