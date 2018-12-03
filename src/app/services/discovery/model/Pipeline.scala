package services.discovery.model

import java.io.StringWriter
import java.util.UUID

import org.apache.jena.rdf.model.Model
import services.discovery.components.analyzer.{EtlSparqlGraphProtocol, LinksetBasedUnion}
import services.discovery.model.components._
import play.api.libs.json._
import play.api.libs.functional.syntax._

case class Pipeline(components: Seq[PipelineComponent], bindings: Seq[PortBinding], lastComponent: PipelineComponent, lastOutputDataSample: DataSample) {
    def isComplete: Boolean = lastComponent.componentInstance.isInstanceOf[ApplicationInstance]

    def prettyFormat(offset: String = ""): String = {
        val formattedBindings = bindings.map(_.prettyFormat).mkString(", ")
        s"${offset}Pipeline(\n$offset  bindings=$formattedBindings\n$offset  lastComponent=${lastComponent.componentInstance}\n$offset)"
    }

    def dataSourceNames = components.filter(_.componentInstance.isInstanceOf[DataSourceInstance]).map(_.componentInstance.asInstanceOf[DataSourceInstance].label).mkString("[", ",", "]")

    def applicationName = lastComponent.componentInstance match {
        case esgp : EtlSparqlGraphProtocol => esgp.label
        case x => s"${x.label} (${x.getClass.getSimpleName})"
    }

    def height: Int = lastComponent.discoveryIteration

    def name: String = s"$dataSourceNames -> $applicationName (${components.size})"

    def datasources = components.filter(_.componentInstance.isInstanceOf[DataSourceInstance])

    def extractors = components.filter(_.componentInstance.isInstanceOf[ExtractorInstance])

    def processors = components.filter(_.componentInstance.isInstanceOf[ProcessorInstance]).filterNot(_.componentInstance.isInstanceOf[ExtractorInstance])

    def transformers = components.filter(_.componentInstance.isInstanceOf[TransformerInstance])

    def applications = components.filter(_.componentInstance.isInstanceOf[ApplicationInstance])

    def typedDatasources = datasources.map(_.componentInstance.asInstanceOf[DataSourceInstance])

    def typedExtractors = extractors.map(_.componentInstance.asInstanceOf[ExtractorInstance])

    def typedProcessors = processors.map(_.componentInstance.asInstanceOf[ProcessorInstance])

    def typedTransformers = transformers.map(_.componentInstance.asInstanceOf[TransformerInstance])

    def typedApplications = applications.map(_.componentInstance.asInstanceOf[ApplicationInstance])

    def dataSample : Model = lastOutputDataSample.getModel(UUID.randomUUID(), height)

    def endsWithLargeDataset = {
        lastComponent.componentInstance match {
            case ci: DataSourceInstance => ci.isLarge
            case _ => false
        }
    }

    def containsInstance(componentInstance: ComponentInstance) : Boolean = {
        components.count(_.componentInstance == componentInstance) > 0
    }

    def containsInstanceOfType[T] = {
        components.exists(_.componentInstance.isInstanceOf[T])
    }

    def endsWith[T] = {
        lastComponent.componentInstance.isInstanceOf[T]
    }

    def endsWith(componentInstance: ComponentInstanceWithInputs): Boolean = {
        lastComponent.componentInstance == componentInstance
    }

}

object Pipeline {

    implicit val writes: Writes[Pipeline] = (
        (JsPath \ "descriptor").write[String] and
            (JsPath \ "name").write[String]
        )(unlift(Pipeline.destruct))

    def destruct(arg: Pipeline): Option[(String, String)] = {
        val datasourcesString = arg.typedDatasources.map(_.label).mkString(",")
        val extractorsString = arg.typedExtractors.map(i => s"${i.label} (${i.getClass.getSimpleName})").mkString(",")
        val transformersString = arg.typedProcessors.map(i => s"${i.label} (${i.getClass.getSimpleName})").mkString(",")
        val transformersCount = arg.typedProcessors.size
        val app = arg.typedApplications.map(i => s"${i.label} (${i.getClass.getSimpleName})").mkString(",")
        val iterationNumber = arg.lastComponent.discoveryIteration

        val descriptor = s"$datasourcesString;$transformersCount;$extractorsString;$transformersString;$app;$iterationNumber"

        Some((descriptor, arg.name))
    }
}