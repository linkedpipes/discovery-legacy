package services.discovery.components.transformer

import java.util.UUID

import services.discovery.components.common.DescriptorChecker
import services.discovery.model._
import services.discovery.model.components.{AskQuery, SparqlQuery, SparqlTransformerInstance, UpdateQuery}

import scala.concurrent.Future

abstract class SparqlUpdateTransformer extends SparqlTransformerInstance with DescriptorChecker {
    val portName = "INPUT"
    val port = Port(portName, 0)

    protected val prefixes : String

    protected val deleteClause : String

    protected val insertClause : String

    protected val whereClause: String

    private lazy val query = UpdateQuery(
        s"""
          |$prefixes
          |
          |DELETE { $deleteClause }
          |INSERT { $insertClause }
          |WHERE { $whereClause }
        """.stripMargin)

    private lazy val descriptor = AskQuery(
        s"""
          |$prefixes
          |
          |ASK { $whereClause }
        """.stripMargin
    )

    override def checkPort(port: Port, state: Option[ComponentState], outputDataSample: DataSample, discoveryId: UUID, iterationNumber: Int): Future[PortCheckResult] = {
        checkStatelessDescriptors(outputDataSample, discoveryId, iterationNumber, descriptor)
    }

    override def getInputPorts: Seq[Port] = Seq(port)

    override def getOutputDataSample(state: Option[ComponentState], dataSamples: Map[Port, DataSample], discoveryId: UUID, iterationNumber: Int): Future[DataSample] = {
        val newSample = dataSamples(port).transform(query, discoveryId, iterationNumber)
        Future.successful(ModelDataSample(newSample))
    }

    override def getQueryByPort(port: Port): SparqlQuery = query
}
