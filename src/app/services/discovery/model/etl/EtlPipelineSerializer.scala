package services.discovery.model.etl

import org.apache.jena.query.Dataset
import org.apache.jena.rdf.model._
import org.apache.jena.vocabulary.RDF
import services.discovery.components.analyzer.{EtlRdf2File, EtlSparqlGraphProtocol}
import services.discovery.components.datasource.{EtlSparqlEndpoint, SparqlEndpoint}
import services.discovery.model._
import services.discovery.model.components.{SparqlEndpointInstance, SparqlUpdateTransformerInstance}

import scala.collection.mutable

case class Config(resource: Resource, model: Model)

case class ConfiguredComponent(resource: Resource, config: Config)

class EtlPipelineSerializer(etlPipeline: Pipeline, endpointUri: String, graphIri: Option[String]) {

    private lazy val iriGenerator = new EtlIriGenerator
    private lazy val pipelineIri = iriGenerator.pipelineIri
    private lazy val dataModel = PipelineDataModel.create(pipelineIri)
    private lazy val iterations = etlPipeline.components.map(_.discoveryIteration).distinct.sorted
    private val rows = new mutable.HashMap[Int, Int]
    val resultGraphIri: String = graphIri.getOrElse(GuidGenerator.nextIri)

    def serialize: Dataset = {
        addPipeline()
        val componentsMap = addComponents()
        addBindings(componentsMap)
        dataModel.dataset
    }

    private def addPipeline(): Unit = {
        val pipelineResource = dataModel.pipelineModel.createResource(pipelineIri)
        pipelineResource.addProperty(prefLabel, etlPipeline.name)
        pipelineResource.addProperty(RDF.`type`, lpPipelineResource)
    }

    private def addComponents(): Map[PipelineComponent, ConfiguredComponent] = {
        etlPipeline.components.map { c =>
            c.componentInstance match {
                case se: SparqlEndpoint => addSparqlEndpoint(c, se)
                case ese: EtlSparqlEndpoint => addSparqlEndpoint(c, ese)
                case t: SparqlUpdateTransformerInstance => addSparqlTransformer(c, t)
                case f: EtlRdf2File => addRdf2File(c, f)
                case gp: EtlSparqlGraphProtocol => addOutput(c, gp)
                case _ => (null, null)
            }
        }.toMap
    }

    private def addSparqlTransformer(pipelineComponent: PipelineComponent, sparqlTransformerInstance: SparqlUpdateTransformerInstance): (PipelineComponent, ConfiguredComponent) = {

        val componentResource = addComponent(pipelineComponent.componentInstance.label, "http://localhost:8080/resources/components/t-sparqlUpdate", pipelineComponent.discoveryIteration)
        val config = createConfig(componentResource, "http://plugins.linkedpipes.com/ontology/t-sparqlUpdate#Configuration")
        val query = sparqlTransformerInstance.getQueryByPort(sparqlTransformerInstance.getInputPorts.head).query
        config.resource.addProperty(config.model.createProperty("http://plugins.linkedpipes.com/ontology/t-sparqlUpdate#query"), query)
        (pipelineComponent, ConfiguredComponent(componentResource, config))
    }

    private def addSparqlEndpoint(pipelineComponent: PipelineComponent, sparqlEndpoint: SparqlEndpointInstance): (PipelineComponent, ConfiguredComponent) = {
        val componentResource = addComponent(sparqlEndpoint.label, "http://localhost:8080/resources/components/e-sparqlEndpoint", pipelineComponent.discoveryIteration)
        val query = sparqlEndpoint match {
            case e: EtlSparqlEndpoint => e.query.query
            case _ => "CONSTRUCT { ?s ?p ?o } WHERE { ?s ?p ?o . }"
        }
        val config = createSparqlEndpointConfig(componentResource, sparqlEndpoint, query)
        (pipelineComponent, ConfiguredComponent(componentResource, config))
    }

    private def createSparqlEndpointConfig(component: Resource, sparqlEndpoint: SparqlEndpointInstance, query: String): Config = {
        val namespace = "http://plugins.linkedpipes.com/ontology/e-sparqlEndpoint#"
        val config = createConfig(component, namespace + "Configuration")
        config.resource.addProperty(config.model.createProperty(namespace, "query"), query)
        config.resource.addProperty(config.model.createProperty(namespace, "endpoint"), sparqlEndpoint.url)
        if (sparqlEndpoint.url.contains("http://dbpedia.org/sparql")) {
            config.resource.addProperty(config.model.createProperty(namespace, "headerAccept"), "text/plain")
        } else {
            config.resource.addProperty(config.model.createProperty(namespace, "headerAccept"), "text/ttl")
        }
        sparqlEndpoint.defaultGraphIris.foreach { iri =>
            config.resource.addProperty(config.model.createProperty(namespace, "defaultGraph"), config.model.createResource(iri))
        }
        config
    }

    private def createConfig(componentResource: Resource, configType: String): Config = {
        val configModel = ModelFactory.createDefaultModel()
        val configIri = iriGenerator.configurationIri(componentResource.getURI)
        val configResource = configModel.createResource(configIri)
        configResource.addProperty(RDF.`type`, configModel.createResource(configType))
        dataModel.dataset.addNamedModel(configIri, configModel)
        componentResource.addProperty(lpConfigurationProperty, dataModel.pipelineModel.createResource(configIri))
        Config(configResource, configModel)
    }

    private def addComponent(label: String, templateUri: String, iterationNumber: Int): Resource = {
        val component = dataModel.pipelineModel.createResource(iriGenerator.componentIri)
        component.addProperty(prefLabel, label)
        component.addProperty(RDF.`type`, lpComponentResource)
        component.addProperty(lpTemplateProperty, dataModel.pipelineModel.createResource(templateUri))
        component.addProperty(lpX, getX(iterationNumber).toString)
        component.addProperty(lpY, getY(iterationNumber).toString)
        component
    }

    private def getX(iterationNumber: Int) = {
        20 + (350 * iterations.indexOf(iterationNumber))
    }

    private def getY(iterationNumber: Int) = {
        val rowCount = rows.getOrElse(iterationNumber, 0) + 1
        rows.put(iterationNumber, rowCount)
        20 + (rowCount * 150)
    }

    private def addRdf2File(pipelineComponent: PipelineComponent, etlRdf2File: EtlRdf2File): (PipelineComponent, ConfiguredComponent) = {
        val rdfToFile = addComponent("RDF to File", "http://localhost:8080/resources/components/t-rdfToFile", pipelineComponent.discoveryIteration)
        val rdf2FileConfig = createRdf2FileConfig(rdfToFile)
        dataModel.dataset.addNamedModel(rdf2FileConfig.resource.getURI, rdf2FileConfig.model)
        (pipelineComponent, ConfiguredComponent(rdfToFile, rdf2FileConfig))
    }

    private def addOutput(pipelineComponent: PipelineComponent, etlSparqlGraphProtocol: EtlSparqlGraphProtocol): (PipelineComponent, ConfiguredComponent) = {
        val graphStore = addComponent("Graph store protocol", "http://localhost:8080/resources/components/l-graphStoreProtocol", pipelineComponent.discoveryIteration)
        val graphStoreConfig = createGraphStoreConfig(graphStore, etlSparqlGraphProtocol)
        dataModel.dataset.addNamedModel(graphStoreConfig.resource.getURI, graphStoreConfig.model)
        (pipelineComponent, ConfiguredComponent(graphStore, graphStoreConfig))
    }

    private def createGraphStoreConfig(componentResource: Resource, etlSparqlGraphProtocol: EtlSparqlGraphProtocol): Config = {
        val namespace = "http://plugins.linkedpipes.com/ontology/l-graphStoreProtocol#"
        val config = createConfig(componentResource, namespace + "Configuration")
        config.resource.addProperty(config.model.createProperty(namespace + "repository"), "BLAZEGRAPH")
        config.resource.addProperty(config.model.createProperty(namespace + "graph"), resultGraphIri)
        config.resource.addProperty(config.model.createProperty(namespace + "endpoint"), s"$endpointUri/sparql")
        config
    }

    private def createRdf2FileConfig(componentResource: Resource): Config = {
        val namespace = "http://plugins.linkedpipes.com/ontology/t-rdfToFile#"
        val config = createConfig(componentResource, namespace + "Configuration")
        config.resource.addProperty(config.model.createProperty(namespace + "fileName"), "data.ttl")
        config.resource.addProperty(config.model.createProperty(namespace + "fileType"), "text/turtle")
        config.resource.addProperty(config.model.createProperty(namespace + "graphUri"), resultGraphIri)
        config
    }

    private def addBindings(componentsMap: Map[PipelineComponent, ConfiguredComponent]): Unit = {
        etlPipeline.bindings.foreach { b =>
            val source = componentsMap(b.startComponent).resource
            val target = componentsMap(b.endComponent).resource

            val outputName = b.startComponent.componentInstance match {
                case r: EtlRdf2File => lpOutputFilesName
                case _ => lpOutputName
            }

            val inputName = b.endComponent.componentInstance match {
                case g: EtlSparqlGraphProtocol => lpInputFilesName
                case _ => lpInputName
            }

            addBinding(source, target, outputName, inputName)
        }
    }

    private def addBinding(source: Resource, target: Resource, sourcePort: String, targetPort: String): Unit = {
        val binding = dataModel.pipelineModel.createResource(iriGenerator.connectionIri)
        binding.addProperty(RDF.`type`, lpConnectionTypeResource)
        binding.addProperty(lpConnectionSourceComponentProperty, source)
        binding.addProperty(lpConnectionTargetComponentProperty, target)
        binding.addProperty(lpConnectionSourceBinding, sourcePort)
        binding.addProperty(lpConnectionTargetBinding, targetPort)
    }

    val lpOutputName = "OutputRdf"
    val lpInputName = "InputRdf"
    val lpOutputFilesName = "OutputFile"
    val lpInputFilesName = "InputFiles"

    val prefLabel = ResourceFactory.createProperty("http://www.w3.org/2004/02/skos/core#prefLabel")
    val lpComponentResource = ResourceFactory.createResource("http://linkedpipes.com/ontology/Component")
    val lpPipelineResource = ResourceFactory.createResource("http://linkedpipes.com/ontology/Pipeline")
    val lpTemplateProperty = ResourceFactory.createProperty("http://linkedpipes.com/ontology/template")
    val lpConfigurationProperty = ResourceFactory.createProperty("http://linkedpipes.com/ontology/configurationGraph")
    val lpConnectionSourceComponentProperty = ResourceFactory.createProperty("http://linkedpipes.com/ontology/sourceComponent")
    val lpConnectionTargetComponentProperty = ResourceFactory.createProperty("http://linkedpipes.com/ontology/targetComponent")
    val lpConnectionSourceBinding = ResourceFactory.createProperty("http://linkedpipes.com/ontology/sourceBinding")
    val lpConnectionTargetBinding = ResourceFactory.createProperty("http://linkedpipes.com/ontology/targetBinding")
    val lpConnectionTypeResource = ResourceFactory.createResource("http://linkedpipes.com/ontology/Connection")
    val lpX = ResourceFactory.createProperty("http://linkedpipes.com/ontology/x")
    val lpY = ResourceFactory.createProperty("http://linkedpipes.com/ontology/y")

}
