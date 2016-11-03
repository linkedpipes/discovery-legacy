package services.discovery.components.visualizer

import java.util.UUID

import services.discovery.components.common.DescriptorChecker
import services.discovery.model.components.{AskQuery, VisualizerInstance}
import services.discovery.model.{ComponentState, DataSample, Port, PortCheckResult}

import scala.concurrent.Future

class TimeVisualizer extends VisualizerInstance with DescriptorChecker {
  val portName: String = "INPUT_PORT"

  private val descriptor = AskQuery(
    """
      |PREFIX time: <http://www.w3.org/2006/time#>
      |
      |ASK {
      |    ?t time:inXSDDateTime ?d .
      |}
    """.stripMargin
  )

  override val getInputPorts: Seq[Port] = Seq(Port(portName, 0))

  override def checkPort(port: Port, state: Option[ComponentState], outputDataSample: DataSample, discoveryId: UUID, iterationNumber: Int): Future[PortCheckResult] = {
    checkStatelessDescriptors(outputDataSample, discoveryId, iterationNumber, descriptor)
  }
}
