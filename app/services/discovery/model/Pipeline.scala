package services.discovery.model

import services.discovery.model.components._

case class Pipeline(components: Seq[PipelineComponent], bindings: Seq[PortBinding], lastComponent: PipelineComponent, lastOutputDataSample: DataSample) {
  def isComplete: Boolean = lastComponent.componentInstance.isInstanceOf[VisualizerInstance]

  def prettyFormat(offset: String = ""): String = {
    val formattedBindings = bindings.map(_.prettyFormat).mkString(", ")
    s"${offset}Pipeline(\n$offset  bindings=$formattedBindings\n$offset  lastComponent=${lastComponent.componentInstance}\n$offset)"
  }

  def endsWith(componentInstance: ComponentInstanceWithInputs): Boolean = {
    lastComponent.componentInstance == componentInstance
  }

  def height: Int = lastComponent.discoveryIteration

  def name: String = "LP-VIZ generated pipeline"

}