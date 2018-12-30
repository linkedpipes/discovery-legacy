package controllers.dto

import controllers.dto.PipelineGrouping.PipelineGroup
import play.api.libs.json.{Json, Writes}
import services.discovery.model.components.ApplicationInstance

import scala.concurrent.ExecutionContext

case class PipelineApplicationGroup(applicationInstance: ApplicationInstance, dataSourceGroups: Seq[PipelineDataSourceGroup])


object PipelineApplicationGroup {

    implicit val writes : Writes[PipelineApplicationGroup] = Json.writes[PipelineApplicationGroup]

    def create(pipelineGroup: PipelineGroup[ApplicationInstance])(implicit executionContext: ExecutionContext) : PipelineApplicationGroup = {

        pipelineGroup match {
            case (appInstance, pipelines) => {
                val groupsByDataSource = pipelines.groupBy(p => p._2.typedDatasources.toSet)
                PipelineApplicationGroup(appInstance, groupsByDataSource.map { g => PipelineDataSourceGroup.create(g) }.toSeq)
            }
        }
    }
}