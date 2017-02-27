package services.discovery.model

import org.apache.jena.rdf.model.{Model, ModelFactory, Resource, ResourceFactory}
import org.apache.jena.vocabulary.RDF
import org.topbraid.spin.model.Construct
import services.discovery.components.application.Application
import services.discovery.components.datasource.SparqlEndpoint
import services.discovery.components.extractor.SparqlConstructExtractor
import services.discovery.components.transformer.SparqlUpdateTransformer
import services.discovery.model.components._

import scala.collection.JavaConverters._

case class DiscoveryInput(
    dataSets: Seq[DataSet],
    extractors: Seq[ExtractorInstance],
    processors: Seq[ProcessorInstance],
    visualizers: Seq[ApplicationInstance]
)

trait RdfVocabulary {
    def prefix: String

    protected def resource(name: String) = ResourceFactory.createResource(s"$prefix$name")

    protected def property(name: String) = ResourceFactory.createProperty(s"$prefix$name")
}

object LDVM extends RdfVocabulary {
    val prefix = "http://linked.opendata.cz/ontology/ldvm/"
    val DataSourceTemplate = resource("DataSourceTemplate")
    val ExtractorTemplate = resource("ExtractorTemplate")
    val TransformerTemplate = resource("TransformerTemplate")
    val ApplicationTemplate = resource("ApplicationTemplate")
    val MandatoryFeature = resource("MandatoryFeature")
    val OptionalFeature = resource("OptionalFeature")
    val Descriptor = resource("Descriptor")

    val componentConfigurationTemplate = property("componentConfigurationTemplate")
    val service = property("service")
    val query = property("query")
    val feature = property("feature")
    val descriptor = property("descriptor")
    val appliesTo = property("appliesTo")
    val outputTemplate = property("outputTemplate")
    val outputDataSample = property("outputDataSample")
}

object SD extends RdfVocabulary {
    val prefix = "http://www.w3.org/ns/sparql-service-description#"

    val endpoint = property("endpoint")
}

case class RelevantModel(importantResources: Seq[Resource])

case class Feature(isMandatory: Boolean, descriptors: Seq[Descriptor])

case class Descriptor(query: AskQuery, port: Resource)

case class DataSet(dataSourceInstance: DataSourceInstance, extractorInstance: Option[ExtractorInstance])

object DiscoveryInput {

    def apply(templates: Seq[Model]): DiscoveryInput = {

        val dataSets = getDataSets(templates)
        val processors = getProcessors(templates)
        val applications = getApplications(templates)

        new DiscoveryInput(dataSets, Seq(), processors, applications)
    }

    private def getDataSets(models: Seq[Model]): Seq[DataSet] = {
        getTemplatesOfType(models, LDVM.DataSourceTemplate).map { template =>
            val configuration = template.getModel.getRequiredProperty(template, LDVM.componentConfigurationTemplate).getObject.asResource()
            val service = configuration.getPropertyResourceValue(LDVM.service)
            val endpointUri = service.getPropertyResourceValue(SD.endpoint).getURI
            val output = template.getRequiredProperty(LDVM.outputTemplate).getResource
            val dataSampleUri = Option(output.getRequiredProperty(LDVM.outputDataSample).getResource.getURI)

            val extractorQuery = Option(configuration.getProperty(LDVM.query)).map(_.getString)
            val extractor = extractorQuery.map(eq => new SparqlConstructExtractor(ConstructQuery(eq)))
            DataSet(SparqlEndpoint(endpointUri, descriptorIri = dataSampleUri), extractor)
        }
    }

    private def getProcessors(models: Seq[Model]): Seq[ProcessorInstance] = {
        getTemplatesOfType(models, LDVM.TransformerTemplate).map { t =>
            val configuration = t.getRequiredProperty(LDVM.componentConfigurationTemplate).getObject.asResource()
            val updateQuery = UpdateQuery(configuration.getRequiredProperty(LDVM.query).getString)
            new SparqlUpdateTransformer(updateQuery, getFeatures(t))
        }
    }

    private def getApplications(models: Seq[Model]): Seq[ApplicationInstance] = {
        getTemplatesOfType(models, LDVM.ApplicationTemplate).map { t =>
            new Application(getFeatures(t))
        }
    }

    private def getTemplatesOfType(models: Seq[Model], template: Resource): Seq[Resource] = {
        models.flatMap(m => m.listResourcesWithProperty(RDF.`type`, template).asScala.toSeq)
    }

    private def getFeatures(template: Resource): Seq[Feature] = {
        template.listProperties(LDVM.feature).toList.asScala.map(_.getObject.asResource()).map { f =>
            val isMandatory = f.getPropertyResourceValue(RDF.`type`).getURI.equals(LDVM.MandatoryFeature.getURI)
            val descriptors = f.listProperties(LDVM.descriptor).toList.asScala.map(_.getObject.asResource()).map { d =>
                Descriptor(AskQuery(d.getRequiredProperty(LDVM.query).getString), d.getRequiredProperty(LDVM.appliesTo).getObject.asResource())
            }
            Feature(isMandatory, descriptors)
        }
    }
}