package services.discovery.components.analyzer

import services.discovery.model.Port
import services.discovery.model.components.ComponentInstance

case class Virtuoso(override val label: String) extends ComponentInstance {
  val port = Port("input", 0)


  override def iri: String = "https://linked.opendata.cz/ontology/analyzer/etl-virtuoso"
}
