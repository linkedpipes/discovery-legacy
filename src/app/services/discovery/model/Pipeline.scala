package services.discovery.model

import services.discovery.components.analyzer.EtlSparqlGraphProtocol
import services.discovery.model.components._

case class Pipeline(components: Seq[PipelineComponent], bindings: Seq[PortBinding], lastComponent: PipelineComponent, lastOutputDataSample: DataSample) {
    def isComplete: Boolean = lastComponent.componentInstance.isInstanceOf[ApplicationInstance]

    def prettyFormat(offset: String = ""): String = {
        val formattedBindings = bindings.map(_.prettyFormat).mkString(", ")
        s"${offset}Pipeline(\n$offset  bindings=$formattedBindings\n$offset  lastComponent=${lastComponent.componentInstance}\n$offset)"
    }

    def endsWith(componentInstance: ComponentInstanceWithInputs): Boolean = {
        lastComponent.componentInstance == componentInstance
    }

    def dataSourceNames = components.filter(_.componentInstance.isInstanceOf[DataSourceInstance]).map(_.componentInstance.asInstanceOf[DataSourceInstance].label).mkString("[", ",", "]")

    def visualizerName = lastComponent.componentInstance.asInstanceOf[EtlSparqlGraphProtocol].name

    def height: Int = lastComponent.discoveryIteration

    def name: String = s"$dataSourceNames -> $visualizerName (${components.size})"

    def datasources = components.filter(_.componentInstance.isInstanceOf[DataSourceInstance])

    def extractors = components.filter(_.componentInstance.isInstanceOf[ExtractorInstance])

    def processors = components.filter(_.componentInstance.isInstanceOf[ProcessorInstance]).filterNot(_.componentInstance.isInstanceOf[ExtractorInstance])

    def transformers = components.filter(_.componentInstance.isInstanceOf[TransformerInstance])

    def visualizers = components.filter(_.componentInstance.isInstanceOf[ApplicationInstance])

    def typedDatasources = datasources.map(_.componentInstance.asInstanceOf[DataSourceInstance])

    def typedExtractors = extractors.map(_.componentInstance.asInstanceOf[ExtractorInstance])

    def typedProcessors = processors.map(_.componentInstance.asInstanceOf[ProcessorInstance])

    def typedTransformers = transformers.map(_.componentInstance.asInstanceOf[TransformerInstance])

    def typedVisualizers = visualizers.map(_.componentInstance.asInstanceOf[ApplicationInstance])

}