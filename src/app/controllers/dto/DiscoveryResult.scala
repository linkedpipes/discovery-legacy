package controllers.dto

import ai.x.play.json.Jsonx

case class DiscoveryResult(pipelineCount: Int, isFinished: Boolean, duration: Long)

object DiscoveryResult {
    implicit lazy val jsonFormat = Jsonx.formatCaseClassUseDefaults[DiscoveryResult]
}