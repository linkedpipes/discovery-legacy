package services.discovery.components.datasource

import java.util.UUID

import ai.x.play.json.Jsonx
import services.discovery.model.components.SparqlEndpointInstance
import services.discovery.model.{ComponentState, DataSample, Port, SparqlEndpointDataSample}

import scala.concurrent.{ExecutionContext, Future}


case class SparqlEndpoint(override val iri: String, url: String, defaultGraphIris: Seq[String] = Seq(), label: String = "", descriptorIri: Option[String] = None, isLarge: Boolean = false, isLinkset: Boolean = false) extends SparqlEndpointInstance {
    override def getOutputDataSample(state: Option[ComponentState], dataSamples: Map[Port, DataSample], discoveryId: UUID, iterationNumber: Int)(implicit executionContext: ExecutionContext): Future[DataSample] = {
        Future.successful(DataSample.apply(this))
    }
}


object SparqlEndpoint {
    implicit lazy val jsonFormat = Jsonx.formatCaseClassUseDefaults[SparqlEndpoint]
}

