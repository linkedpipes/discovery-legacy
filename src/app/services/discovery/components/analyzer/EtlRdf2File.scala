package services.discovery.components.analyzer

import services.discovery.model.Port
import services.discovery.model.components.ComponentInstance

case class EtlRdf2File() extends ComponentInstance {
  val port = Port("input", 0)

  override def iri: String = "https://linked.opendata.cz/ontology/analyzer/etl-rdf2file"

  override def label: String = "RDF to File"
}
