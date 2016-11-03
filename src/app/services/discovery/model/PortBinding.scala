package services.discovery.model

case class PortBinding(startComponent: PipelineComponent, endPort: Port, endComponent: PipelineComponent) {
  def prettyFormat = s"\n$startComponent --${endPort.name}-> ${endComponent.componentInstance.getClass.getSimpleName}"
}
