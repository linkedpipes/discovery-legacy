package services.discovery.components.extractor

import java.util.UUID

import services.discovery.components.common.DescriptorChecker
import services.discovery.model._
import services.discovery.model.components.{AskQuery, ConstructQuery, SparqlExtractorInstance, SparqlQuery}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class PopulatedPlacesExtractor extends SparqlExtractorInstance with DescriptorChecker {
  val portName: String = "INPUT_PORT"
  val port = Port(portName, 0)

  val query = ConstructQuery(
    """
      | PREFIX dbo: <http://dbpedia.org/ontology/>
      | PREFIX dbp: <http://dbpedia.org/property/>
      |
      | CONSTRUCT {
      |    ?p a dbo:PopulatedPlace ;
      |         dbo:populationTotal ?population ;
      |         dbp:officialName ?on .
      | } WHERE {
      |    ?p a dbo:PopulatedPlace ;
      |         dbo:populationTotal ?population ;
      |         dbp:officialName ?on .
      | }
    """.stripMargin
  )

  private val descriptor = AskQuery(
    """
      | PREFIX dbo: <http://dbpedia.org/ontology/>
      | PREFIX dbp: <http://dbpedia.org/property/>
      |
      | ASK {
      |    ?p a dbo:PopulatedPlace ;
      |         dbo:populationTotal ?population ;
      |         dbp:officialName ?on .
      | }
    """.stripMargin
  )

  override def getOutputDataSample(state: Option[ComponentState], dataSamples: Map[Port, DataSample], discoveryId: UUID, iterationNumber: Int): Future[DataSample] = {
    dataSamples(port).executeConstruct(query, discoveryId, iterationNumber).map(ModelDataSample)
  }

  override def checkPort(port: Port, state: Option[ComponentState], outputDataSample: DataSample, discoveryId: UUID, iterationNumber: Int): Future[PortCheckResult] = {
    checkStatelessDescriptors(outputDataSample, discoveryId, iterationNumber, descriptor)
  }

  override val getInputPorts: Seq[Port] = Seq(port)

  override def getQueryByPort(port: Port): SparqlQuery = query
}
