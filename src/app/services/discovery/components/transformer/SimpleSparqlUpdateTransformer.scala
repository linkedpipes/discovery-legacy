package services.discovery.components.transformer

import java.util.UUID

import services.discovery.components.DescriptorChecker
import services.discovery.model._
import services.discovery.model.components._

import scala.concurrent.Future

abstract class SimpleSparqlUpdateTransformer extends SparqlUpdateTransformerInstance with DescriptorChecker {
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
        try {
            val newSample = dataSamples(port).transform(query, discoveryId, iterationNumber)
            Future.successful(ModelDataSample(newSample))
        } catch {
            case e: Throwable => {
                throw e
            }
        }
    }

    override def getQueryByPort(port: Port): SparqlQuery = query
}