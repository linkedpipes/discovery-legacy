package services

import play.api.libs.json.Json

case class ExecutionStatus (
    isQueued: Boolean,
    isRunning: Boolean,
    isFinished: Boolean,
    isCancelled: Boolean,
    isCancelling: Boolean,
    isFailed: Boolean
)

object ExecutionStatus {
    implicit val format = Json.format[ExecutionStatus]
}