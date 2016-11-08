package services.discovery.components.visualizer

import java.util.UUID

import services.discovery.components.common.DescriptorChecker
import services.discovery.model.components.{AskQuery, VisualizerInstance}
import services.discovery.model.{ComponentState, DataSample, Port, PortCheckResult}

import scala.concurrent.Future

abstract class SimpleVisualizer extends VisualizerInstance with DescriptorChecker {
    val portName: String = "INPUT_PORT"

    protected val prefixes: String

    protected val whereClause: String

    private lazy val descriptor = AskQuery(
        s"""
           |$prefixes
           |
           |ASK {
           |    $whereClause
           |}""".stripMargin
    )

    override val getInputPorts: Seq[Port] = Seq(Port(portName, 0))

    override def checkPort(port: Port, state: Option[ComponentState], outputDataSample: DataSample, discoveryId: UUID, iterationNumber: Int): Future[PortCheckResult] = {
        checkStatelessDescriptors(outputDataSample, discoveryId, iterationNumber, descriptor)
    }
}
