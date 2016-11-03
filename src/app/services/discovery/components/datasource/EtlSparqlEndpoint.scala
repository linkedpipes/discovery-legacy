package services.discovery.components.datasource

import java.util.UUID

import services.discovery.model.{ComponentState, DataSample, Port, RdfDataSample}
import services.discovery.model.components.{SparqlEndpointInstance, SparqlQuery}

import scala.concurrent.Future

case class EtlSparqlEndpoint(
  url: String,
  defaultGraphIris: Seq[String],
  query: SparqlQuery,
  label: String
) extends SparqlEndpointInstance {
  override def isLarge: Boolean = false

  override def isLinkset: Boolean = false

  override def getOutputDataSample(state: Option[ComponentState], dataSamples: Map[Port, DataSample], discoveryId: UUID, iterationNumber: Int): Future[DataSample] = {
    Future.successful(RdfDataSample(""))
  }
}
