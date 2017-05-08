package controllers.dto

import java.util.UUID
import play.api.libs.json._
import services.discovery.model.{DataSample, Pipeline}
import play.api.libs.functional.syntax._

case class PipelineDataSampleGroup(dataSample: DataSample, minimalIteration: Int, pipelines: Map[UUID, Pipeline])

object PipelineDataSampleGroup {
    implicit val writes: Writes[PipelineDataSampleGroup] = (
        (JsPath \ "minimalIteration").write[Int] and
            (JsPath \ "pipelineId").write[String] and
            (JsPath \ "pipeline").write[Pipeline]
    )(unlift(PipelineDataSampleGroup.destruct))

    def destruct(arg: PipelineDataSampleGroup): Option[(Int, String, Pipeline)] = {
        val representant = arg.pipelines.toSeq.sortBy(_._2.lastComponent.discoveryIteration).head
        Some((arg.minimalIteration, representant._1.toString, representant._2))
    }
}