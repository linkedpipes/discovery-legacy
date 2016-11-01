package services.discovery.model

case class PortCheckResult(status: PortCheckResult.Status.Status, maybeState: Option[ComponentState] = None)

object PortCheckResult {

  def apply(success: Boolean, state: Option[ComponentState]): PortCheckResult = {
    success match {
      case true => PortCheckResult(Status.Success, state)
      case false => PortCheckResult(Status.Failure, state)
    }
  }

  object Status extends Enumeration {
    type Status = Value
    val Success, Failure, Error = Value
  }

}
