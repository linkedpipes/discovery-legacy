package services.discovery.model

import org.apache.jena.rdf.model.{Model, Resource}
import org.apache.jena.vocabulary.{DCTerms, RDF}
import play.api.libs.functional.syntax.unlift
import play.api.libs.json.{JsPath, Writes}
import services.discovery.components.application.Application
import services.discovery.components.datasource.SparqlEndpoint
import services.discovery.components.extractor.SparqlConstructExtractor
import services.discovery.components.transformer.SparqlUpdateTransformer
import services.discovery.model.components._
import play.api.libs.functional.syntax._
import services.vocabulary.{LDCP, SD}

import scala.collection.JavaConverters._

case class DiscoveryInput(
    dataSets: Seq[DataSet],
    extractors: Seq[ExtractorInstance],
    processors: Seq[ProcessorInstance],
    applications: Seq[ApplicationInstance]
)

case class RelevantModel(importantResources: Seq[Resource])

case class Feature(isMandatory: Boolean, descriptors: Seq[Descriptor])

case class Descriptor(query: AskQuery, port: Resource)

case class DataSet(dataSourceInstance: DataSourceInstance, extractorInstance: Option[ExtractorInstance])

object DiscoveryInput {

    implicit val writes : Writes[DiscoveryInput] = (
        (JsPath \ "datasets").write[Map[String, DataSourceInstance]] and
            (JsPath \ "processors").write[Map[String, ProcessorInstance]] and
            (JsPath \ "applications").write[Map[String, ApplicationInstance]]
        )(unlift(DiscoveryInput.destruct))

    def destruct(i: DiscoveryInput) : Option[(Map[String, DataSourceInstance], Map[String, ProcessorInstance], Map[String, ApplicationInstance])] = {
        Some((
            i.dataSets.map(d => (d.dataSourceInstance.iri, d.dataSourceInstance)).toMap,
            i.processors.map(p => (p.iri, p)).toMap,
            i.applications.map(a => (a.iri, a)).toMap
        ))
    }

    def apply(templateModels: Seq[Model]): DiscoveryInput = {
        val dataSets = getDataSets(templateModels)
        val processors = getProcessors(templateModels)
        val applications = getApplications(templateModels)

        new DiscoveryInput(dataSets, Seq(), processors, applications)
    }

    private def getDataSets(models: Seq[Model]): Seq[DataSet] = {
        getTemplatesOfType(models, LDCP.DataSourceTemplate).map { template =>
            val configuration = template.getModel.getRequiredProperty(template, LDCP.componentConfigurationTemplate).getObject.asResource()
            val service = configuration.getPropertyResourceValue(LDCP.service)
            val endpointUri = service.getPropertyResourceValue(SD.endpoint).getURI
            val output = template.getRequiredProperty(LDCP.outputTemplate).getResource
            val dataSampleUri = Option(output.getRequiredProperty(LDCP.outputDataSample).getResource.getURI)
            val label = template.getProperty(DCTerms.title).getString

            val extractorQuery = Option(configuration.getProperty(LDCP.query)).map(_.getString)
            val extractor = extractorQuery.map(eq => new SparqlConstructExtractor(s"${template.getURI}#extractor", ConstructQuery(eq), s"$label extractor"))
            DataSet(SparqlEndpoint(template.getURI, endpointUri, descriptorIri = dataSampleUri, label = label), extractor)
        }
    }

    private def getProcessors(models: Seq[Model]): Seq[ProcessorInstance] = {
        getTemplatesOfType(models, LDCP.TransformerTemplate).map { template =>
            val label = template.getProperty(DCTerms.title).getString
            val configuration = template.getRequiredProperty(LDCP.componentConfigurationTemplate).getObject.asResource()
            val updateQuery = UpdateQuery(configuration.getRequiredProperty(LDCP.query).getString)
            new SparqlUpdateTransformer(template.getURI, updateQuery, getFeatures(template), label)
        }
    }

    private def getApplications(models: Seq[Model]): Seq[ApplicationInstance] = {
        getTemplatesOfType(models, LDCP.ApplicationTemplate).map { template =>
            val label = template.getProperty(DCTerms.title).getString
            val executorUri = template.getProperty(LDCP.executor).getResource.getURI
            new Application(template.getURI, executorUri, getFeatures(template), label)
        }
    }

    private def getTemplatesOfType(models: Seq[Model], templateType: Resource): Seq[Resource] = {
        models.flatMap(m => m.listResourcesWithProperty(RDF.`type`, templateType).asScala.toSeq)
    }

    private def getFeatures(template: Resource): Seq[Feature] = {
        template.listProperties(LDCP.feature).toList.asScala.map(_.getObject.asResource()).map { f =>
            val isMandatory = f.getPropertyResourceValue(RDF.`type`).getURI.equals(LDCP.MandatoryFeature.getURI)
            val descriptors = f.listProperties(LDCP.descriptor).toList.asScala.map(_.getObject.asResource()).map { d =>
                Descriptor(AskQuery(d.getRequiredProperty(LDCP.query).getString), d.getRequiredProperty(LDCP.appliesTo).getObject.asResource())
            }
            Feature(isMandatory, descriptors)
        }
    }
}