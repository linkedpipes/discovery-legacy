package services.discovery.model

case class PortMatch(port: Port, startPipeline: Pipeline, maybeState: Option[ComponentState])
