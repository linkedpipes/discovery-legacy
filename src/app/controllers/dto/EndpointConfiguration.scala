package controllers.dto

import com.typesafe.config.Config
import play.api.ConfigLoader

case class SparqlEndpointDefinition(repository: String, endpointUri: String, credentials: Option[SparqlEndpointCredentials] = None)

case class SparqlEndpointCredentials(user: String, password: String)

case class SparqlEndpointGraph(endpoint: SparqlEndpointDefinition, iri: String)

object SparqlEndpointDefinition {

    implicit val configLoader: ConfigLoader[SparqlEndpointDefinition] = new ConfigLoader[SparqlEndpointDefinition] {
        def load(rootConfig: Config, path: String): SparqlEndpointDefinition = {
            val config = rootConfig.getConfig(path)
            SparqlEndpointDefinition(
                repository = config.getString("repository"),
                endpointUri = config.getString("uri")
            )
        }
    }
}