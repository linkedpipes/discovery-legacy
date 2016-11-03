package services.discovery.model

trait ComponentState

case class StringComponentState(data: String) extends ComponentState