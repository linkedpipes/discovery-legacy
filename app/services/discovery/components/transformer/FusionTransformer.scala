package services.discovery.components.transformer

import java.util.UUID

import services.discovery.components.common.DescriptorChecker
import services.discovery.model._
import services.discovery.model.components._

import scala.concurrent.Future

class FusionTransformer extends SparqlTransformerInstance with DescriptorChecker {
  val portName = "INPUT"
  val port = Port(portName, 0)
  private val query = UpdateQuery(
    """
      | PREFIX owl: <http://www.w3.org/2002/07/owl#>
      |
      | DELETE { ?entity1 owl:sameAs ?entity2 . }
      | INSERT {
      |   ?entity2 ?prop1 ?subj1 .
      |   ?entity2 ?prop2 ?subj2 .
      | }
      | WHERE {
      |   ?entity1 owl:sameAs ?entity2 ;
      |      ?prop1 ?subj1 .
      |   ?entity2 ?prop2 ?subj2 .
      |   FILTER (?prop1 != owl:sameAs)
      | }
    """.stripMargin)

  private val descriptor = AskQuery(
    """
      |PREFIX owl: <http://www.w3.org/2002/07/owl#>
      |
      |ASK {
      |   ?entity1 owl:sameAs ?entity2 ;
      |       ?prop1 ?subj1 .
      |   ?entity2 ?prop2 ?subj2 .
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
