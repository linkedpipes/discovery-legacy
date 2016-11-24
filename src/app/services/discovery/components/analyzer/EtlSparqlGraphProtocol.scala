package services.discovery.components.analyzer

import services.discovery.model.Port
import services.discovery.model.components.ComponentInstance

case class EtlSparqlGraphProtocol(name: String) extends ComponentInstance {
  val port = Port("input", 0)
}
