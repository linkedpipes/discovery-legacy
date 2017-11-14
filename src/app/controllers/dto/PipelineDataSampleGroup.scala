package controllers.dto

import java.util.UUID
import play.api.libs.json._
import services.discovery.model.{DataSample, Pipeline}
import play.api.libs.functional.syntax._

case class PipelineDataSampleGroup(dataSample: DataSample, minimalIteration: Int, pipelines: Map[UUID, Pipeline])

object PipelineDataSampleGroup {
    implicit val writes: Writes[PipelineDataSampleGroup] = (
        (JsPath \ "minimalIteration").write[Int] and
            (JsPath \ "pipeline").write[Pipeline] and
            (JsPath \ "pipeline" \ "id").write[String]
    )(unlift(PipelineDataSampleGroup.destruct))

    def destruct(arg: PipelineDataSampleGroup): Option[(Int, Pipeline, String)] = {
        val representant = arg.pipelines.toSeq.minBy(_._2.lastComponent.discoveryIteration)
        Some((arg.minimalIteration, representant._2, representant._1.toString))
    }
}