package services.discovery.model

import services.discovery.model.components.ComponentInstance

case class PipelineComponent(id: String, componentInstance: ComponentInstance, discoveryIteration: Int)
