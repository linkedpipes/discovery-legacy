package services.discovery.components.extractor

import java.util.UUID

import services.discovery.components.common.DescriptorChecker
import services.discovery.model._
import services.discovery.model.components.{AskQuery, ConstructQuery, SparqlExtractorInstance, SparqlQuery}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

abstract class SimpleExtractor extends SparqlExtractorInstance with DescriptorChecker {

    val portName: String = "INPUT_PORT"

    val port = Port(portName, 0)

    protected val prefixes: String

    protected val whereClause: String

    protected val constructClause: String

    private lazy val query = ConstructQuery(
        s"""
          |$prefixes
          |
          |CONSTRUCT {
          |    $constructClause
          |} WHERE {
          |    $whereClause
          |}
        """.stripMargin)

    private lazy val descriptor = AskQuery(
        s"""
          |$prefixes
          |ASK {
          |    $whereClause
          |}""".stripMargin
    )

    override def getOutputDataSample(state: Option[ComponentState], dataSamples: Map[Port, DataSample], discoveryId: UUID, iterationNumber: Int): Future[DataSample] = {
        dataSamples(port).executeConstruct(query, discoveryId, iterationNumber).map(m => ModelDataSample(m))
    }

    override def checkPort(port: Port, state: Option[ComponentState], outputDataSample: DataSample, discoveryId: UUID, iterationNumber: Int): Future[PortCheckResult] = {
        checkStatelessDescriptors(outputDataSample, discoveryId, iterationNumber, descriptor)
    }

    override val getInputPorts: Seq[Port] = Seq(port)

    override def getQueryByPort(port: Port): SparqlQuery = query
}
