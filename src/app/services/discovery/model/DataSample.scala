package services.discovery.model

import java.util.UUID
import java.util.concurrent.TimeUnit

import org.apache.jena.query._
import org.apache.jena.rdf.model.{Model, ModelFactory}
import org.apache.jena.update.UpdateAction
import play.Logger
import services.RdfUtils
import services.discovery.JenaUtil
import services.discovery.components.datasource.SparqlEndpoint
import services.discovery.model.components._

import scala.collection.JavaConversions._
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import scala.io.Source
import better.files._

trait DataSample {

    def executeAsk(descriptor: AskQuery, discoveryId: UUID, iterationNumber: Int): Future[Boolean]

    def executeSelect(descriptor: SelectQuery, discoveryId: UUID, iterationNumber: Int): Future[ResultSet]

    def executeConstruct(descriptor: ConstructQuery, discoveryId: UUID, iterationNumber: Int): Future[Model]

    def getModel(discoveryId: UUID, iterationNumber: Int): Model

    def transform(query: UpdateQuery, discoveryId: UUID, iterationNumber: Int): Model = {
        val resultModel = ModelFactory.createDefaultModel()
        resultModel.add(getModel(discoveryId, iterationNumber))
        UpdateAction.execute(query.updateRequest, resultModel)
        resultModel
    }
}

object DataSample {

    def apply(endpoint: SparqlEndpoint): DataSample = {
        endpoint match {
            case e if e.descriptorIri.isEmpty => SparqlEndpointDataSample(e)
            case e if e.descriptorIri.isDefined => {
                val data = Source.fromURL(e.descriptorIri.get)
                DataSample(data.mkString)
            }
        }
    }

    def apply(rdfData: String): DataSample = ModelDataSample(JenaUtil.modelFromTtl(rdfData))
}

case class SparqlEndpointDataSample(sparqlEndpoint: SparqlEndpoint) extends DataSample {
    private val discoveryLogger = Logger.of("discovery")

    override def executeAsk(descriptor: AskQuery, discoveryId: UUID, iterationNumber: Int): Future[Boolean] = {
        withLogger(descriptor, e => e.execAsk, discoveryId, iterationNumber)
    }

    override def executeSelect(descriptor: SelectQuery, discoveryId: UUID, iterationNumber: Int): Future[ResultSet] = {
        withLogger(descriptor, e => e.execSelect, discoveryId, iterationNumber)
    }

    override def executeConstruct(descriptor: ConstructQuery, discoveryId: UUID, iterationNumber: Int): Future[Model] = {
        withLogger(descriptor, e => e.execConstruct, discoveryId, iterationNumber)
    }

    override def getModel(discoveryId: UUID, iterationNumber: Int): Model = {
        sparqlEndpoint.isLarge match {
            case true => ModelFactory.createDefaultModel()
            case false => Await.result(executeConstruct(ConstructQuery("CONSTRUCT { ?s ?p ?o } WHERE { ?s ?p ?o }"), discoveryId, iterationNumber), Duration(30, TimeUnit.MINUTES))
        }
    }

    private def withLogger[T](descriptor: BasicSparqlQuery, execCommand: QueryExecution => T, discoveryId: UUID, iterationNumber: Int): Future[T] = {
        Future.successful {
            val queryId = UUID.randomUUID()
            discoveryLogger.trace(s"[$discoveryId][$iterationNumber][datasample][$queryId] Querying ${sparqlEndpoint.url}: ${descriptor.query.serialize().replaceAll("[\r\n]", "")}.")
            val execution = QueryExecutionFactory.sparqlService(sparqlEndpoint.url, descriptor.query, sparqlEndpoint.defaultGraphIris, sparqlEndpoint.defaultGraphIris)
            val result = execCommand(execution)
            discoveryLogger.trace(s"[$discoveryId][$iterationNumber][datasample][$queryId] Query result: $result.")
            result
        }
    }
}

case class ModelDataSample(f: File) extends DataSample {

    private val discoveryLogger = Logger.of("discovery")

    override def executeAsk(descriptor: AskQuery, discoveryId: UUID, iterationNumber: Int): Future[Boolean] = executeQuery(descriptor, qe => qe.execAsk())

    override def executeConstruct(descriptor: ConstructQuery, discoveryId: UUID, iterationNumber: Int): Future[Model] = executeQuery(descriptor, qe => qe.execConstruct())

    override def executeSelect(descriptor: SelectQuery, discoveryId: UUID, iterationNumber: Int): Future[ResultSet] = executeQuery(descriptor, qe => qe.execSelect())

    override def getModel(discoveryId: UUID, iterationNumber: Int): Model = _model

    private def _model : Model = {
        try
        {
            val data = Source.fromFile(f.toJava, "UTF-8")
            RdfUtils.modelFromTtl(data.getLines().mkString("\n"))
        } catch {
            case e: Exception => {
                discoveryLogger.error(s"Unable to read from ${f.path} (${f.name}): ${e.getMessage} (${e.getCause.getMessage}).")
                throw e
            }
        }
    }

    private def executeQuery[R](descriptor: BasicSparqlQuery, executionCommand: QueryExecution => R): Future[R] = {
        Future.successful {
            val result = executionCommand(QueryExecutionFactory.create(descriptor.query, _model))
            result
        }
    }
}

object ModelDataSample {
    def Empty = ModelDataSample(ModelFactory.createDefaultModel())

    def apply(model: Model) : ModelDataSample = {
        val ttl = RdfUtils.modelToTtl(model)

        val dir = createFolder
        val f = File.newTemporaryFile(parent = Some(dir))
        f.writeText(ttl)

        ModelDataSample(f)
    }

    private def createFolder = {
        val uuid = UUID.randomUUID().toString

        val first6 = uuid.substring(0,5)
        val fragments = first6.mkString("/")

        File(s"/data/tmp/discovery/$fragments").createDirectoryIfNotExists(true)
    }
}