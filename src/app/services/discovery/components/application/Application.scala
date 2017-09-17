package services.discovery.components.application

import java.util.UUID

import services.discovery.components.DescriptorChecker
import services.discovery.model._
import services.discovery.model.components.ApplicationInstance

import scala.concurrent.Future

class Application(override val uri: String, val executorUri: String, features: Seq[Feature], override val label: String) extends ApplicationInstance with DescriptorChecker {
    override def checkPort(port: Port, state: Option[ComponentState], outputDataSample: DataSample, discoveryId: UUID, iterationNumber: Int): Future[PortCheckResult] = {
        checkStatelessDescriptors(outputDataSample, discoveryId, iterationNumber, features.filter(_.isMandatory).flatMap(_.descriptors).map(_.query): _*)
    }

    override def getInputPorts: Seq[Port] = {
        val portUris = features.flatMap(_.descriptors.map(_.port.getURI)).distinct
        portUris.map(pUri => Port(pUri, 0))
    }
}
