package services

import java.util.UUID
import javax.inject._

import controllers.dto.{DiscoveryStatus, CsvRequestData}
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

@Singleton
class DiscoveryService {

    private val discoveries = new scala.collection.mutable.HashMap[UUID, Discovery]
    private val csvRequests = new scala.collection.mutable.HashMap[UUID, Seq[CsvRequestData]]
    private val discoveryLogger = Logger.of("discovery")

    def start(input: DiscoveryInput) = {
        val discovery = Discovery.create(input)
        discoveries.put(discovery.id, discovery)
        discovery.start
        discovery.id
    }

    def addCsvRequest(indexes: Seq[CsvRequestData]) : UUID = {
        val id = UUID.randomUUID()
        csvRequests.put(id, indexes)
        id
    }

    def getCsvRequest(id: UUID) = csvRequests.get(id)

    def stop(id: String) = {}

    def runExperiment(templateUris: Seq[String], templateGrouping: Map[String, List[String]]): UUID = {
        val templates = templateUris.par.map { u => RdfUtils.modelFromIri(u)(discoveryLogger) { e => e } }.filter(_.isRight).map(_.right.get).seq
        val discoveryInput = DiscoveryInput(templates, templateGrouping)
        start(discoveryInput)
    }

    def getPipelinesOfDiscovery(discoveryId: String): Option[mutable.HashMap[UUID, Pipeline]] = withDiscovery(discoveryId) { discovery =>
        discovery.results
    }

    def runExperimentFromInputIri(inputIri: String) : UUID = {
        RdfUtils.modelFromIri(inputIri)(discoveryLogger) {
            case Right(model) => runExperiment(extractTemplates(model), getGroupings(model))
            case _ => runExperiment(Seq(), Map())
        }
    }

    def getExperimentsInputIrisFromIri(iri: String) : Seq[String] = {
        RdfUtils.modelFromIri(iri)(discoveryLogger) {
            case Right(model) => extractExperiments(model)
            case _ => Seq()
        }
    }

    def getExperimentsInputIris(ttlData: String) : Seq[String] = {
        extractExperiments(RdfUtils.modelFromTtl(ttlData))
    }

    def runExperimentFromInput(ttlData: String) : UUID = {
        val model = RdfUtils.modelFromTtl(ttlData)
        runExperiment(extractTemplates(model), getGroupings(model))
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

    def listTemplates(templateSourceUri: String): Either[Throwable, DiscoveryInput] = {
        RdfUtils.modelFromIri(templateSourceUri)(discoveryLogger) {
            case Right(model) => {
                val templates = model.listObjectsOfProperty(LPD.hasTemplate).toList.asScala
                val templateIris = templates.map(_.asResource().getURI)
                val templateModels = templateIris.par.map { u => RdfUtils.modelFromIri(u)(discoveryLogger) { e => e } }.filter(_.isRight).map(_.right.get).toList
                Right(DiscoveryInput(templateModels, getGroupings(model)))
            }
            case Left(e) => Left(e)
        }
    }

    private def getGroupings(model: Model) : Map[String, List[String]] = {
        val templateGroups = model.listSubjectsWithProperty(RDF.`type`, LPD.TransformerGroup).asScala.toList
        templateGroups.map { tg =>
            (tg.asResource().getURI, model.listObjectsOfProperty(tg, LPD.hasTransformer).asScala.toList.map(_.asResource().getURI))
        }.toMap
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
