package services.discovery.components.extractor

import java.util.UUID

import services.discovery.components.DescriptorChecker
import services.discovery.model._
import services.discovery.model.components._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class SparqlConstructExtractor(query: ConstructQuery) extends SparqlConstructExtractorInstance with DescriptorChecker {

    val portName: String = "INPUT_PORT"

    val port = Port(portName, 0)

    override def getOutputDataSample(state: Option[ComponentState], dataSamples: Map[Port, DataSample], discoveryId: UUID, iterationNumber: Int): Future[DataSample] = {
        dataSamples(port).executeConstruct(ConstructQuery(query.query), discoveryId, iterationNumber).map(m => ModelDataSample(m))
    }

    override def checkPort(port: Port, state: Option[ComponentState], outputDataSample: DataSample, discoveryId: UUID, iterationNumber: Int): Future[PortCheckResult] = {
        checkStatelessDescriptors(outputDataSample, discoveryId, iterationNumber, query.descriptor)
    }

    override val getInputPorts: Seq[Port] = Seq(port)

    override def getQueryByPort(port: Port): SparqlQuery = query
}
