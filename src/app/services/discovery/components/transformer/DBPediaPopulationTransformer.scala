package services.discovery.components.transformer

import java.util.UUID

import services.discovery.components.common.DescriptorChecker
import services.discovery.model._
import services.discovery.model.components.{AskQuery, SparqlQuery, SparqlTransformerInstance, UpdateQuery}

import scala.concurrent.Future

class DBPediaPopulationTransformer extends SparqlTransformerInstance with DescriptorChecker {
  val portName = "INPUT"
  val port = Port(portName, 0)
  private val query = UpdateQuery(
    """
      | PREFIX owl: <http://www.w3.org/2002/07/owl#>
      | PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
      | PREFIX dbo: <http://dbpedia.org/ontology/>
      |
      | DELETE { ?place dbo:populationTotal ?pop . }
      | INSERT { ?place rdf:value ?pop . }
      | WHERE { ?place dbo:populationTotal ?pop . }
      |‚
    """.stripMargin)

  private val descriptor = AskQuery(
    """
      |PREFIX dbo: <http://dbpedia.org/ontology/>
      |
      |ASK {
      |   ?place dbo:populationTotal ?pop .
      |}
    """.stripMargin
  )

  override def checkPort(port: Port, state: Option[ComponentState], outputDataSample: DataSample, discoveryId: UUID, iterationNumber: Int): Future[PortCheckResult] = {
    checkStatelessDescriptors(outputDataSample, discoveryId, iterationNumber, descriptor)
  }

  override def getInputPorts: Seq[Port] = Seq(port)

  override def getOutputDataSample(state: Option[ComponentState], dataSamples: Map[Port, DataSample], discoveryId: UUID, iterationNumber: Int): Future[DataSample] = {
    val newSample = dataSamples(port).transform(query)
    Future.successful(ModelDataSample(newSample))
  }

  override def getQueryByPort(port: Port): SparqlQuery = query
}
