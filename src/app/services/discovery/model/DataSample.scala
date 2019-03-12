package services.discovery.model

import java.util.UUID

import org.apache.jena.query._
import org.apache.jena.rdf.model.{Model, ModelFactory}
import org.apache.jena.update.UpdateAction
import play.Logger
import services.RdfUtils
import services.discovery.JenaUtil
import services.discovery.components.datasource.SparqlEndpoint
import services.discovery.model.components._

import scala.collection.JavaConversions._
import scala.concurrent.{ExecutionContext, Future}
import scala.io.Source
import better.files._

trait DataSample {

    def executeAsk(descriptor: AskQuery)(implicit executionContext: ExecutionContext): Future[Boolean]

    def executeSelect(descriptor: SelectQuery)(implicit executionContext: ExecutionContext): Future[ResultSet]

    def executeConstruct(descriptor: ConstructQuery)(implicit executionContext: ExecutionContext): Future[Model]

    def getModel(implicit executionContext: ExecutionContext):Future[Model]

    def transform(query: UpdateQuery, discoveryId: UUID, iterationNumber: Int)(implicit executionContext: ExecutionContext): Future[Model] = {
        Future {
            val resultModel = ModelFactory.createDefaultModel()
            getModel.map { m =>
                resultModel.add(m)
                UpdateAction.execute(query.updateRequest, resultModel)
            }
            resultModel
        }
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

    override def executeAsk(descriptor: AskQuery)(implicit executionContext: ExecutionContext): Future[Boolean] = {
        withLogger(descriptor, e => e.execAsk)
    }

    override def executeSelect(descriptor: SelectQuery)(implicit executionContext: ExecutionContext): Future[ResultSet] = {
        withLogger(descriptor, e => e.execSelect)
    }

    override def executeConstruct(descriptor: ConstructQuery)(implicit executionContext: ExecutionContext): Future[Model] = {
        withLogger(descriptor, e => e.execConstruct)
    }

    override def getModel(implicit executionContext: ExecutionContext): Future[Model] = {
        sparqlEndpoint.isLarge match {
            case true => Future.successful(ModelFactory.createDefaultModel())
            case false => executeConstruct(ConstructQuery("CONSTRUCT { ?s ?p ?o } WHERE { ?s ?p ?o }"))
        }
    }

    private def withLogger[T](descriptor: BasicSparqlQuery, execCommand: QueryExecution => T)(implicit executionContext: ExecutionContext): Future[T] = {
        Future {
            val queryId = UUID.randomUUID()
            discoveryLogger.trace(s"[datasample][$queryId] Querying ${sparqlEndpoint.url}: ${descriptor.query.serialize().replaceAll("[\r\n]", "")}.")
            val execution = QueryExecutionFactory.sparqlService(sparqlEndpoint.url, descriptor.query, sparqlEndpoint.defaultGraphIris, sparqlEndpoint.defaultGraphIris)
            val result = execCommand(execution)
            discoveryLogger.trace(s"[datasample][$queryId] Query result: $result.")
            result
        }
    }
}

case class ModelDataSample(f: File) extends DataSample {

    private val discoveryLogger = Logger.of("discovery")

    override def executeAsk(descriptor: AskQuery)(implicit executionContext: ExecutionContext): Future[Boolean] = executeQuery(descriptor, qe => qe.execAsk())

    override def executeConstruct(descriptor: ConstructQuery)(implicit executionContext: ExecutionContext): Future[Model] = executeQuery(descriptor, qe => qe.execConstruct())

    override def executeSelect(descriptor: SelectQuery)(implicit executionContext: ExecutionContext): Future[ResultSet] = executeQuery(descriptor, qe => qe.execSelect())

    override def getModel(implicit executionContext: ExecutionContext): Future[Model] = _model

    private def _model(implicit executionContext: ExecutionContext) : Future[Model] = {
        Future {
            try
            {
                RdfUtils.modelFromTtl(f)
            } catch {
                case e: Exception => {
                    discoveryLogger.error("Exception: " + e.getClass.getCanonicalName)
                    discoveryLogger.error(s"Unable to read from ${f.path} (${f.name}): ${e.getMessage} (${e.getCause.getMessage}).")
                    throw e
                }
            }
        }
    }

    private def executeQuery[R](descriptor: BasicSparqlQuery, executionCommand: QueryExecution => R)(implicit executionContext: ExecutionContext): Future[R] = {
        _model.map { m =>
            executionCommand(QueryExecutionFactory.create(descriptor.query, m))
        }
    }
}

object ModelDataSample {
    private val discoveryLogger = Logger.of("discovery")

    def Empty = ModelDataSample(ModelFactory.createDefaultModel())

    def apply(model: Model) : ModelDataSample = {
        try
        {
            val ttl = RdfUtils.modelToTtl(model)
            val dir = createFolder
            val f = File.newTemporaryFile(parent = Some(dir))
            f.writeText(ttl)
            ModelDataSample(f)
        } catch {
            case e: Exception => {
                discoveryLogger.error("Exception: " + e.getClass.getCanonicalName)
                discoveryLogger.error(s"Unable to write TTL into a file.")
                throw e
            }
        }
    }

    private def createFolder = {
        val uuid = UUID.randomUUID().toString

        val first6 = uuid.substring(0,5)
        val fragments = first6.mkString("/")

        File(s"/tmp/data/tmp/discovery/$fragments").createDirectoryIfNotExists(true)
    }
}