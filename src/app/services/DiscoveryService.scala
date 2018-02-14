package services

import java.util.UUID

import controllers.dto.DiscoveryStatus
import models.ExecutionResult
import org.apache.jena.rdf.model.{AnonId, Model, ModelFactory, Resource}
import org.apache.jena.vocabulary.RDF
import play.Logger
import services.discovery.Discovery
import services.discovery.model.etl.{EtlPipeline, EtlPipelineExporter}
import services.discovery.model.{DiscoveryInput, Pipeline}
import services.vocabulary.{ETL, LPD}

import scala.collection.JavaConverters._
import scala.collection.mutable


class DiscoveryService {

    private val discoveries = new scala.collection.mutable.HashMap[UUID, Discovery]
    private val csvRequests = new scala.collection.mutable.HashMap[UUID, Seq[(String, String)]]
    private val discoveryLogger = Logger.of("discovery")

    def start(input: DiscoveryInput) = {
        val discovery = Discovery.create(input)
        discoveries.put(discovery.id, discovery)
        discovery.start
        discovery.id
    }

    def addCsvRequest(data: Seq[(String, String)]) : UUID = {
        val id = UUID.randomUUID()
        csvRequests.put(id, data)
        id
    }

    def getCsvRequest(id: UUID) = csvRequests.get(id)

    def stop(id: String) = {}

    def runExperiment(templateUris: Seq[String]): UUID = {
        val templates = templateUris.par.map { u => RdfUtils.fromIri(u)(discoveryLogger) { e => e } }.filter(_.isRight).map(_.right.get).seq
        val discoveryInput = DiscoveryInput(templates)
        start(discoveryInput)
    }

    def getPipelinesOfDiscovery(discoveryId: String): Option[mutable.HashMap[UUID, Pipeline]] = withDiscovery(discoveryId) { discovery =>
        discovery.results
    }

    def runExperimentFromInputIri(inputIri: String) : UUID = {
        val iris = RdfUtils.fromIri(inputIri)(discoveryLogger) {
            case Right(model) => extractTemplates(model)
            case _ => Seq()
        }
        runExperiment(iris)
    }

    def getExperimentsInputIrisFromIri(iri: String) : Seq[String] = {
        RdfUtils.fromIri(iri)(discoveryLogger) {
            case Right(model) => extractExperiments(model)
            case _ => Seq()
        }
    }

    def getExperimentsInputIris(ttlData: String) : Seq[String] = {
        extractExperiments(RdfUtils.fromTtl(ttlData))
    }

    def runExperimentFromInput(ttlData: String) : UUID = {
        runExperiment(extractTemplates(RdfUtils.fromTtl(ttlData)))
    }

    def extractTemplates(model: Model) : Seq[String] = {
        val templates = model.listObjectsOfProperty(LPD.hasTemplate).toList.asScala
        templates.map(_.asResource().getURI)
    }

    def extractExperiments(model: Model) : Seq[String] = {
        val list = model.listObjectsOfProperty(LPD.hasDiscovery).toList.asScala
        val head = Option(list.head.asResource())
        head match {
            case Some(listHead) => extractList(model, listHead)
            case None => Seq()
        }
    }

    def extractList(model: Model, listHead: Resource) : Seq[String] = {
        var currentHead = listHead
        val buffer = new mutable.ArrayBuffer[String]()

        do {
            buffer.+=(currentHead.getRequiredProperty(RDF.first).getResource.getURI)
            currentHead = currentHead.getRequiredProperty(RDF.rest).getResource
        } while (!currentHead.equals(RDF.nil))

        buffer
    }

    def getStatus(id: String): Option[DiscoveryStatus] = withDiscovery(id) { discovery =>
        DiscoveryStatus(
            discovery.results.size,
            discovery.isFinished,
            discovery.duration
        )
    }

    def getEtlPipeline(id: String, pipelineId: String, endpointUri: String): Option[EtlPipeline] = {
        withPipeline(id, pipelineId) { (p, d) =>
            EtlPipelineExporter.export(p, endpointUri)
        }
    }

    def getDataSampleService(pipelineId: String, discoveryId: String, dataSample: String, host: String, endpointUri: String) : Model = {
        val graphUri = s"urn:$discoveryId/$pipelineId"
        RdfUtils.graphStoreBlazegraph(dataSample, endpointUri, graphUri)
        service(pipelineId, discoveryId, host, endpointUri, graphUri)
    }

    def getService(executionResult: ExecutionResult, host: String, endpointUri: String): Model = {
        service(executionResult.pipelineId, executionResult.discoveryId, host, endpointUri, executionResult.graphIri)
    }

    def service(pipelineId: String, discoveryId: String, host: String, endpointUri: String, graphIri: String) : Model = {
        val servicePrefix = "http://www.w3.org/ns/sparql-service-description#"

        val model = ModelFactory.createDefaultModel()

        val namedGraph = model.createResource(AnonId.create())
        namedGraph.addProperty(RDF.`type`, model.createResource(s"${servicePrefix}NamedGraph"))
        namedGraph.addProperty(model.createProperty(s"${servicePrefix}name"), model.createResource(graphIri))

        val dataset = model.createResource(AnonId.create())
        dataset.addProperty(RDF.`type`, model.createResource(s"${servicePrefix}Dataset"))
        dataset.addProperty(model.createProperty(s"${servicePrefix}namedGraph"), namedGraph)

        val service = model.createResource(s"http://$host/discovery/$discoveryId/$pipelineId/service")
        service.addProperty(RDF.`type`, model.createResource(s"${servicePrefix}Service"))
        service.addProperty(model.createProperty(s"${servicePrefix}endpoint"), model.createResource(s"$endpointUri/sparql"))
        service.addProperty(model.createProperty(s"${servicePrefix}supportedLanguage"), model.createResource(s"${servicePrefix}SPARQL11Query"))
        service.addProperty(model.createProperty(s"${servicePrefix}resultFormat"), model.createResource("http://www.w3.org/ns/formats/RDF_XML"))
        service.addProperty(model.createProperty(s"${servicePrefix}resultFormat"), model.createResource("http://www.w3.org/ns/formats/Turtle"))
        service.addProperty(model.createProperty(s"${servicePrefix}defaultDataset"), dataset)

        model
    }

    def listTemplates(templateSourceUri: String): Option[DiscoveryInput] = {
        RdfUtils.fromIri(templateSourceUri)(discoveryLogger) {
            case Right(model) => {
                val templates = model.listObjectsOfProperty(LPD.hasTemplate).toList.asScala
                val templateUris = templates.map(_.asResource().getURI)
                val templateModels = templateUris.map { u => RdfUtils.fromIri(u)(discoveryLogger) { e => e } }.filter(_.isRight).map(_.right.get)
                Some(DiscoveryInput(templateModels))
            }
            case Left(e) => None
        }
    }

    def executionStatus(iri: String): ExecutionStatus = {
        RdfUtils.fromJsonLd(s"$iri/overview") { d =>

            val model = d.getDefaultModel
            val statusNodes = model.listObjectsOfProperty(ETL.executionStatus).toList.asScala

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

    def withDiscovery[R](id: String)(action: Discovery => R): Option[R] = {
        val uuid = UUID.fromString(id)
        discoveries.get(uuid).map(action)
    }

    def withPipeline[R](discoveryId: String, pipelineId: String)(action: (Pipeline, Discovery) => R): Option[R] = {
        withDiscovery(discoveryId) { discovery =>
            val uuid = UUID.fromString(pipelineId)
            discovery.results.get(uuid).map { p => action(p, discovery) }
        }.flatten
    }

}
