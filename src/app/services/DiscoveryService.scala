package services

import java.util.UUID

import controllers.dto.DiscoveryResult
import org.apache.jena.query.Dataset
import org.apache.jena.rdf.model.{AnonId, Model, ModelFactory}
import org.apache.jena.riot.{Lang, RDFDataMgr, RiotException}
import org.apache.jena.vocabulary.RDF
import play.Logger
import services.discovery.Discovery
import services.discovery.model.etl.{EtlPipeline, EtlPipelineExporter}
import services.discovery.model.{DiscoveryInput, Pipeline}

import scala.collection.JavaConverters._
import scala.collection.mutable


class DiscoveryService {

    private val discoveries = new scala.collection.mutable.HashMap[UUID, Discovery]
    private val etlPipelines = new scala.collection.mutable.HashMap[Pipeline, EtlPipeline]
    private val discoveryLogger = Logger.of("discovery")

    def start(input: DiscoveryInput) = {
        val discovery = Discovery.create
        discoveries.put(discovery.id, discovery)
        discovery.discover(input)
        discovery.id
    }

    def stop(id: String) = {}

    def runExperiment(templateUris: Seq[String]): UUID = {
        val templates = templateUris.map { u => fromUri(u) { e => e } }.filter(_.isRight).map(_.right.get)
        val discoveryInput = DiscoveryInput(templates)
        start(discoveryInput)
    }

    def getStatus(id: String): Option[DiscoveryResult] = withDiscovery(id) { discovery =>
        DiscoveryResult(
            discovery.results.size,
            discovery.isFinished,
            (discovery.end - discovery.start) / (1000 * 1000) // ns -> ms
        )
    }

    def getEtlPipeline(id: String, pipelineId: String, endpointUri: String): Option[EtlPipeline] = {
        withPipeline(id, pipelineId) { (p, d) =>
            val etlPipeline = EtlPipelineExporter.export(p, endpointUri)
            etlPipelines.put(p, etlPipeline)
            etlPipeline
        }
    }

    def getPipelines(id: String): Option[mutable.HashMap[UUID, Pipeline]] = withDiscovery(id) { discovery =>
        discovery.results
    }

    def getService(discoveryId: String, pipelineId: String, host: String, endpointUri: String): Option[Model] = {
        withPipeline(discoveryId, pipelineId) { (p, d) =>
            etlPipelines.get(p).map { ep =>

                val servicePrefix = "http://www.w3.org/ns/sparql-service-description#"

                val model = ModelFactory.createDefaultModel()

                val namedGraph = model.createResource(AnonId.create())
                namedGraph.addProperty(RDF.`type`, model.createResource(s"${servicePrefix}NamedGraph"))
                namedGraph.addProperty(model.createProperty(s"${servicePrefix}name"), model.createResource(ep.resultGraphIri))

                val dataset = model.createResource(AnonId.create())
                dataset.addProperty(RDF.`type`, model.createResource(s"${servicePrefix}Dataset"))
                dataset.addProperty(model.createProperty(s"${servicePrefix}namedGraph"), namedGraph)

                val service = model.createResource(s"${host}/discovery/${discoveryId}/${pipelineId}/service")
                service.addProperty(RDF.`type`, model.createResource(s"${servicePrefix}Service"))
                service.addProperty(model.createProperty(s"${servicePrefix}endpoint"), model.createResource(endpointUri))
                service.addProperty(model.createProperty(s"${servicePrefix}supportedLanguage"), model.createResource(s"${servicePrefix}SPARQL11Query"))
                service.addProperty(model.createProperty(s"${servicePrefix}resultFormat"), model.createResource("http://www.w3.org/ns/formats/RDF_XML"))
                service.addProperty(model.createProperty(s"${servicePrefix}resultFormat"), model.createResource("http://www.w3.org/ns/formats/Turtle"))
                service.addProperty(model.createProperty(s"${servicePrefix}defaultDataset"), dataset)

                model
            }
        }.flatten
    }

    def listTemplates(templateSourceUri: String): Option[DiscoveryInput] = {
        fromUri(templateSourceUri) {
            case Right(model) => {
                val templates = model.listObjectsOfProperty(model.getProperty("http://linked.opendata.cz/ldcp/property/hasTemplate")).toList.asScala
                val templateUris = templates.map(_.asResource().getURI)
                val templateModels = templateUris.map { u => fromUri(u) { e => e } }.filter(_.isRight).map(_.right.get)
                Some(DiscoveryInput(templateModels))
            }
            case Left(e) => None
        }
    }

    def executionStatus(iri: String): ExecutionStatus = {
        fromJsonLd(s"$iri/overview") { d =>

            val model = d.getDefaultModel
            val statusNodes = model.listObjectsOfProperty(model.createProperty("http://etl.linkedpipes.com/ontology/executionStatus")).toList.asScala

            def is(statusName: String): Boolean = statusNodes.exists(n => n.asResource().getURI.equals(s"http://etl.linkedpipes.com/resources/status/$statusName"))

            ExecutionStatus(
                isQueued = is("queued"),
                isRunning = is("running"),
                isCancelling = is("cancelling"),
                isCancelled = is("cancelled"),
                isFinished = is("finished"),
                isFailed = is("failed")
            )
        }
    }

    private def withDiscovery[R](id: String)(action: Discovery => R): Option[R] = {
        val uuid = UUID.fromString(id)
        discoveries.get(uuid).map(action)
    }

    private def withPipeline[R](discoveryId: String, pipelineId: String)(action: (Pipeline, Discovery) => R): Option[R] = {
        withDiscovery(discoveryId) { discovery =>
            val uuid = UUID.fromString(pipelineId)
            discovery.results.get(uuid).map { p => action(p, discovery) }
        }.flatten
    }

    private def fromJsonLd[R](iri: String)(fn: Dataset => R): R = {
        val dataset = RDFDataMgr.loadDataset(iri, Lang.JSONLD)
        fn(dataset)
    }

    private def fromUri[R](uri: String)(fn: Either[Throwable, Model] => R): R = {
        discoveryLogger.debug(s"Downloading data from $uri.")
        val result = try {
            val model = ModelFactory.createDefaultModel()
            model.read(uri)
            Right(model)
        } catch {
            case e: RiotException => Left(new Exception(s"The data at $uri caused the following error: ${e.getMessage}."))
        }
        fn(result)
    }

}
