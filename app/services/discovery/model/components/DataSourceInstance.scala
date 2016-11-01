package services.discovery.model.components

trait DataSourceInstance extends ComponentInstanceWithOutput {

  def isLarge: Boolean

  def isLinkset: Boolean

  def label: String

}

trait SparqlEndpointInstance extends DataSourceInstance {

  def url: String

  def defaultGraphIris: Seq[String]

}
