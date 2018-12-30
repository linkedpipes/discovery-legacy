package services.discovery.components.transformer

import java.util.UUID

import services.discovery.components.DescriptorChecker
import services.discovery.model._
import services.discovery.model.components._

import scala.concurrent.{ExecutionContext, Future}

class SparqlUpdateTransformer(override val iri: String, query: UpdateQuery, features: Seq[Feature], override val label: String, override val transformerGroupIri: Option[String])(implicit executionContext: ExecutionContext) extends SparqlUpdateTransformerInstance with DescriptorChecker {

    val ports = features.flatMap(_.descriptors.map(_.port.getURI)).distinct.map(pUri => Port(pUri, 0))

    override def checkPort(port: Port, state: Option[ComponentState], outputDataSample: DataSample, discoveryId: UUID, iterationNumber: Int): Future[PortCheckResult] = {
        checkStatelessDescriptors(outputDataSample, discoveryId, iterationNumber, features.filter(_.isMandatory).flatMap(_.descriptors).map(_.query): _*)
    }

    override def getInputPorts: Seq[Port] = ports.headOption.toSeq

    override def getOutputDataSample(state: Option[ComponentState], dataSamples: Map[Port, DataSample], discoveryId: UUID, iterationNumber: Int)(implicit executionContext: ExecutionContext): Future[DataSample] = {
        try {
            val newSample = dataSamples(ports.head).transform(query, discoveryId, iterationNumber)
            newSample.map(s => ModelDataSample(s))
        } catch {
            case e: Throwable => {
                throw e
            }
        }
    }

    override def getQueryByPort(port: Port): UpdateQuery = query
}
