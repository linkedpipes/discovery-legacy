package services.discovery.model

import java.util.UUID

import org.apache.jena.query.{QueryExecution, QueryExecutionFactory, QueryFactory, ResultSet}
import org.apache.jena.rdf.model.{Model, ModelFactory}
import org.apache.jena.update.UpdateAction
import play.Logger
import services.discovery.JenaUtil
import services.discovery.components.datasource.SparqlEndpoint
import services.discovery.model.components._

import scala.collection.JavaConversions._
import scala.concurrent.Future

trait DataSample {

    def executeAsk(descriptor: AskQuery, discoveryId: UUID, iterationNumber: Int): Future[Boolean]

    def executeSelect(descriptor: SelectQuery, discoveryId: UUID, iterationNumber: Int): Future[ResultSet]

    def executeConstruct(descriptor: ConstructQuery, discoveryId: UUID, iterationNumber: Int): Future[Model]

    def getModel: Model

    def transform(query: UpdateQuery): Model = {
        val resultModel = ModelFactory.createDefaultModel()
        resultModel.add(getModel)
        UpdateAction.parseExecute(query.query, resultModel)
        resultModel
    }
}

case class SparqlEndpointDataSample(sparqlEndpoint: SparqlEndpoint) extends DataSample {
    private val discoveryLogger = Logger.of("discovery")

    override def executeAsk(descriptor: AskQuery, discoveryId: UUID, iterationNumber: Int): Future[Boolean] = withLogger(descriptor, e => e.execAsk, discoveryId, iterationNumber)

    override def executeSelect(descriptor: SelectQuery, discoveryId: UUID, iterationNumber: Int): Future[ResultSet] = withLogger(descriptor, e => e.execSelect, discoveryId, iterationNumber)

    override def executeConstruct(descriptor: ConstructQuery, discoveryId: UUID, iterationNumber: Int): Future[Model] = withLogger(descriptor, e => e.execConstruct, discoveryId, iterationNumber)

    override def getModel: Model = ModelFactory.createDefaultModel()

    private def withLogger[T](descriptor: SparqlQuery, execCommand: QueryExecution => T, discoveryId: UUID, iterationNumber: Int) : Future[T] = {
        Future.successful {
            val queryId = UUID.randomUUID()
            discoveryLogger.trace(s"[$discoveryId][$iterationNumber][datasample][$queryId] Querying ${sparqlEndpoint.url}: ${descriptor.query.replaceAll("[\r\n]", "")}.")
            val q = QueryFactory.create(descriptor.query)
            val execution = QueryExecutionFactory.sparqlService(sparqlEndpoint.url, q, sparqlEndpoint.defaultGraphIris, sparqlEndpoint.defaultGraphIris)
            val result = execCommand(execution)
            discoveryLogger.trace(s"[$discoveryId][$iterationNumber][datasample][$queryId] Query result: $result.")
            result
        }
    }
}

case class ModelDataSample(model: Model) extends DataSample {
    override def executeAsk(descriptor: AskQuery, discoveryId: UUID, iterationNumber: Int): Future[Boolean] = {
        Future.successful {
            val query = QueryFactory.create(descriptor.query)
            val execution = QueryExecutionFactory.create(query, model)
            execution.execAsk()
        }
    }

    override def executeConstruct(descriptor: ConstructQuery, discoveryId: UUID, iterationNumber: Int): Future[Model] = {
        Future.successful {
            val query = QueryFactory.create(descriptor.query)
            val execution = QueryExecutionFactory.create(query, model)
            execution.execConstruct()
        }
    }

    override def executeSelect(descriptor: SelectQuery, discoveryId: UUID, iterationNumber: Int): Future[ResultSet] =
        Future.successful {
            val query = QueryFactory.create(descriptor.query)
            val execution = QueryExecutionFactory.create(query, model)
            execution.execSelect()
        }

    override def getModel: Model = model
}

case class RdfDataSample(rdfData: String) extends DataSample {
    private val model = JenaUtil.modelFromTtl(rdfData)
    private val modelSample = ModelDataSample(model)

    override def executeAsk(descriptor: AskQuery, discoveryId: UUID, iterationNumber: Int): Future[Boolean] = {
        modelSample.executeAsk(descriptor, discoveryId: UUID, iterationNumber: Int)
    }

    override def executeSelect(descriptor: SelectQuery, discoveryId: UUID, iterationNumber: Int): Future[ResultSet] = {
        modelSample.executeSelect(descriptor, discoveryId, iterationNumber)
    }

    override def executeConstruct(descriptor: ConstructQuery, discoveryId: UUID, iterationNumber: Int): Future[Model] = {
        modelSample.executeConstruct(descriptor, discoveryId, iterationNumber)
    }

    override def getModel: Model = model
}

case object EmptyDataSample extends DataSample {
    override def executeAsk(query: AskQuery, discoveryId: UUID, iterationNumber: Int): Future[Boolean] = Future.successful(false)

    override def executeConstruct(query: ConstructQuery, discoveryId: UUID, iterationNumber: Int): Future[Model] = ???

    override def executeSelect(query: SelectQuery, discoveryId: UUID, iterationNumber: Int): Future[ResultSet] = ???

    override def getModel: Model = ???
}
