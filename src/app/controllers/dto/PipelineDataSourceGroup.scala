package controllers.dto

import controllers.dto.PipelineGrouping.PipelineGroup
import play.api.libs.json.{Json, Writes}
import services.discovery.model.components.DataSourceInstance

import scala.concurrent.ExecutionContext

case class PipelineDataSourceGroup(dataSourceInstances: Set[DataSourceInstance], extractorGroups: Seq[PipelineExtractorGroup])


object PipelineDataSourceGroup {

    implicit val writes : Writes[PipelineDataSourceGroup] = Json.writes[PipelineDataSourceGroup]

    def create(pipelineGroup: PipelineGroup[Set[DataSourceInstance]])(implicit executionContext: ExecutionContext) : PipelineDataSourceGroup = {
        pipelineGroup match {
            case (dsInstances, pipelines) => {
                val groupsByExtractor = pipelines.groupBy(p => p._2.typedExtractors.toSet)
                PipelineDataSourceGroup(dsInstances, groupsByExtractor.map { g => PipelineExtractorGroup.create(g) }.toSeq)
            }
        }
    }
}