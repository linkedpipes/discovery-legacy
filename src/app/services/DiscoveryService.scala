package services

import java.util.UUID

import javax.inject._
import controllers.dto._
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

import better.files._
import java.io.{File => JFile}

@Singleton
class DiscoveryService @Inject()(
    statisticsService: StatisticsService
){

    private val discoveries = new scala.collection.mutable.HashMap[UUID, Discovery]
    private val discoveryLogger = Logger.of("discovery")

    def start(input: DiscoveryInput) : Discovery = {
        val discovery = Discovery.create(input)
        discoveries.put(discovery.id, discovery)
        discovery.start
        discovery
    }

    def stop(id: String) = {}

    def start(templateUris: Seq[String], templateGrouping: Map[String, List[String]] = Map()): Discovery = {
        val templates = templateUris.par.map { u => RdfUtils.modelFromIri(u)(discoveryLogger) { e => e } }.filter(_.isRight).map(_.right.get).seq
        val discoveryInput = DiscoveryInput(templates, templateGrouping)
        start(discoveryInput)
    }

    def startExperimentFromIri(experimentIri: String) : Unit = {
        val discoveryInputIris = getDiscoveryInputIrisFromExperimentIri(experimentIri)
        startNextDiscovery(0, discoveryInputIris, experimentIri.split("/").last)
    }

    def startNextDiscovery(i: Int, discoveryInputIris: Seq[String], expId: String): Unit = {
        val nextIri = discoveryInputIris(i)
        val discovery = startFromInputIri(nextIri)
        discovery.onStop += { d =>
            println(s"========= Running discovery #$i has finished.")

            val zip = statisticsService.getZip(Seq(CsvRequestData(nextIri, d.id.toString)), this)
            s"D:\\mff-experiments\\exp-${expId.toString}\\dis-${"%03d".format(i)}-${discovery.id}.results.zip".toFile.createFileIfNotExists(createParents = true).writeByteArray(zip.toByteArray)

            discoveries.clear()
            if (i+1 < discoveryInputIris.size) {
                startNextDiscovery(i+1, discoveryInputIris, expId)
            }
        }
    }

    def getPipelinesOfDiscovery(discoveryId: String): Option[mutable.HashMap[UUID, Pipeline]] = withDiscovery(discoveryId) { discovery =>
        discovery.results
    }

    def startFromInputIri(inputIri: String) : Discovery = {
        RdfUtils.modelFromIri(inputIri)(discoveryLogger) {
            case Right(model) => start(extractTemplates(model), getGroupings(model))
            case _ => start(Seq(), Map())
        }
    }

    def getDiscoveryInputIrisFromExperimentIri(iri: String) : Seq[String] = {
        RdfUtils.modelFromIri(iri)(discoveryLogger) {
            case Right(model) => extractDiscoveryInputs(model)
            case _ => Seq()
        }
    }

    def getDiscoveryInputIrisFromExperiment(ttlData: String) : Seq[String] = {
        extractDiscoveryInputs(RdfUtils.modelFromTtl(ttlData))
    }

    def startFromInput(ttlData: String) : Discovery = {
        val model = RdfUtils.modelFromTtl(ttlData)
        start(extractTemplates(model), getGroupings(model))
    }

    def extractTemplates(model: Model) : Seq[String] = {
        val templates = model.listObjectsOfProperty(LPD.hasTemplate).toList.asScala
        templates.map(_.asResource().getURI)
    }

    def extractDiscoveryInputs(model: Model) : Seq[String] = {
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

    def getEtlPipeline(pipelineKey: PipelineKey, endpointGraph: SparqlEndpointGraph): Option[EtlPipeline] = {
        withPipeline(pipelineKey) { (p, d) =>
            EtlPipelineExporter.export(p, endpointGraph)
        }
    }

    def storeDataSample(dataSample: Model, sparqlEndpointDefinition: SparqlEndpointDefinition, discoveryId: String, pipelineId: String) : SparqlEndpointGraph = {
        sparqlEndpointDefinition.repository match {
            case "BLAZEGRAPH" => {
                val graph = SparqlEndpointGraph(sparqlEndpointDefinition, s"urn:$discoveryId/$pipelineId")
                RdfUtils.graphStoreBlazegraph(dataSample, graph)
                graph
            }
            case _ => throw new NotImplementedError()
        }
    }

    def getDataSampleService(hostname: String, graph: SparqlEndpointGraph, discoveryId: String, pipelineId: String) : Model = {
        service(hostname: String, graph: SparqlEndpointGraph, discoveryId, pipelineId)
    }

    def getService(endpoint: SparqlEndpointDefinition, executionResult: ExecutionResult, hostname: String): Model = {
        val graph = SparqlEndpointGraph(endpoint, executionResult.graphIri)
        service(hostname, graph, executionResult.discoveryId, executionResult.pipelineId)
    }

    def service(hostname: String, graph: SparqlEndpointGraph, discoveryId: String, pipelineId: String) : Model = {
        val servicePrefix = "http://www.w3.org/ns/sparql-service-description#"

        val model = ModelFactory.createDefaultModel()

        val namedGraph = model.createResource(AnonId.create())
        namedGraph.addProperty(RDF.`type`, model.createResource(s"${servicePrefix}NamedGraph"))
        namedGraph.addProperty(model.createProperty(s"${servicePrefix}name"), model.createResource(graph.iri))

        val dataset = model.createResource(AnonId.create())
        dataset.addProperty(RDF.`type`, model.createResource(s"${servicePrefix}Dataset"))
        dataset.addProperty(model.createProperty(s"${servicePrefix}namedGraph"), namedGraph)

        val service = model.createResource(s"http://$hostname/discovery/$discoveryId/$pipelineId/service")
        service.addProperty(RDF.`type`, model.createResource(s"${servicePrefix}Service"))
        service.addProperty(model.createProperty(s"${servicePrefix}endpoint"), model.createResource(s"${graph.endpoint.endpointUri}/sparql"))
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

    def withPipeline[R](pipelineKey: PipelineKey)(action: (Pipeline, Discovery) => R): Option[R] = {
        withDiscovery(pipelineKey.discoveryId) { discovery =>
            val uuid = UUID.fromString(pipelineKey.pipelineId)
            discovery.results.get(uuid).map { p => action(p, discovery) }
        }.flatten
    }

}
