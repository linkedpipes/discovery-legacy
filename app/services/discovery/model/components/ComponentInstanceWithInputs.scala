package services.discovery.model.components

import java.util.UUID

import services.discovery.model._

import scala.concurrent.Future


trait ComponentInstanceWithInputs extends ComponentInstance {

  def checkPort(port: Port, state: Option[ComponentState], outputDataSample: DataSample, discoveryId: UUID, iterationNumber: Int): Future[PortCheckResult]

  def getInputPorts : Seq[Port]
}
