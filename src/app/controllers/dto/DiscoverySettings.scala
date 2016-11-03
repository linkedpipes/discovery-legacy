package controllers.dto

import ai.x.play.json.Jsonx
import services.discovery.components.datasource.SparqlEndpoint

case class DiscoverySettings(combineExistingDataSources: Boolean = false, sparqlEndpoints: Seq[SparqlEndpoint] = Seq())

object DiscoverySettings {
    implicit lazy val jsonFormat = Jsonx.formatCaseClassUseDefaults[DiscoverySettings]
}
