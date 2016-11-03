package services.discovery.components.visualizer

import java.util.UUID

import services.discovery.components.common.DescriptorChecker
import services.discovery.model._
import services.discovery.model.components.{AskQuery, VisualizerInstance}

import scala.concurrent.Future

class PopulationVisualizer extends VisualizerInstance with DescriptorChecker {
  val portName = "INPUT"

  private val descriptor = AskQuery(
    """
      |PREFIX s: <http://schema.org/>
      |PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
      |
      |    ASK {
      |      ?p rdf:value ?populationCount ;
      |         s:name ?placeName ;
      |         s:geo [
      |           s:latitude ?lat ;
      |           s:longitude ?lng
      |         ]
      |      .
      |    }
    """.stripMargin
  )

  override def checkPort(port: Port, state: Option[ComponentState], outputDataSample: DataSample, discoveryId: UUID, iterationNumber: Int): Future[PortCheckResult] = {
    checkStatelessDescriptors(outputDataSample, discoveryId, iterationNumber, descriptor)
  }

  override def getInputPorts: Seq[Port] = Seq(Port(portName, 0))
}
