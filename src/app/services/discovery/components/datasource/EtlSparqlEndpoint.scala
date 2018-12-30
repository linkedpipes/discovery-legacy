package services.discovery.components.datasource

import java.util.UUID

import services.discovery.model._
import services.discovery.model.components.{SparqlEndpointInstance, SparqlQuery}

import scala.concurrent.{ExecutionContext, Future}

case class EtlSparqlEndpoint(
    url: String,
    defaultGraphIris: Seq[String],
    query: SparqlQuery,
    override val label: String
) extends SparqlEndpointInstance {
    override def isLarge: Boolean = false

    override def isLinkset: Boolean = false

    override def getOutputDataSample(state: Option[ComponentState], dataSamples: Map[Port, DataSample], discoveryId: UUID, iterationNumber: Int)(implicit executionContext: ExecutionContext): Future[DataSample] = {
        Future.successful(ModelDataSample.Empty)
    }

    override def iri: String = "https://linked.opendata.cz/ontology/datasource/etl-sparql-endpoint"
}
