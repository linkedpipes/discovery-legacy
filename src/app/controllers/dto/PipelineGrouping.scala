package controllers.dto

import java.util.UUID

import play.api.libs.json.{Json, Writes}
import services.discovery.model.Pipeline

import scala.collection.mutable
import scala.concurrent.ExecutionContext

case class PipelineGrouping(applicationGroups: Seq[PipelineApplicationGroup], pipelines: mutable.HashMap[UUID, Pipeline])

object PipelineGrouping {

    implicit val writes : Writes[PipelineGrouping] = Json.writes[PipelineGrouping]

    type PipelineGroup[T] = (T, mutable.HashMap[UUID, Pipeline])

    def create(pipelines: mutable.HashMap[UUID, Pipeline])(implicit executionContext: ExecutionContext) : PipelineGrouping = {
        val groups = pipelines.groupBy(p => p._2.typedApplications.head)
        PipelineGrouping(groups.map { g => PipelineApplicationGroup.create(g) }.toSeq, pipelines)
    }
}
