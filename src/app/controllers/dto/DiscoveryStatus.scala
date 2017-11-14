package controllers.dto

import ai.x.play.json.Jsonx

case class DiscoveryStatus(pipelineCount: Int, isFinished: Boolean, duration: Long)

object DiscoveryStatus {
    implicit lazy val jsonFormat = Jsonx.formatCaseClassUseDefaults[DiscoveryStatus]
}