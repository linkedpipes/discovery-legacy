package controllers.dto

import java.util.UUID
import java.util.concurrent.TimeUnit

import controllers.dto.PipelineGrouping.PipelineGroup
import play.api.libs.json.{Json, Writes}
import services.discovery.model.{DataSample, Pipeline}
import services.discovery.model.components.ExtractorInstance

import scala.collection.mutable
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}

case class PipelineExtractorGroup(extractorInstances: Set[ExtractorInstance], dataSampleGroups: Seq[PipelineDataSampleGroup])


object PipelineExtractorGroup {

    implicit val writes : Writes[PipelineExtractorGroup] = Json.writes[PipelineExtractorGroup]

    def create(pipelineGroup: PipelineGroup[Set[ExtractorInstance]])(implicit executionContext: ExecutionContext): PipelineExtractorGroup = {

        pipelineGroup match {
            case (extractorInstances, pipelines) => {
                PipelineExtractorGroup(extractorInstances, groupPipelinesByDataSample(pipelines))
            }
        }
    }

    def groupPipelinesByDataSample(pipelines: mutable.HashMap[UUID, Pipeline])(implicit executionContext: ExecutionContext) : IndexedSeq[PipelineDataSampleGroup] = {
        var sortedPipelines = pipelines.toIndexedSeq.sortBy(p => p._2.lastComponent.discoveryIteration)

        val groups = new mutable.ArrayBuffer[PipelineDataSampleGroup]
        while (sortedPipelines.nonEmpty) {
            val basePipeline = sortedPipelines.head
            val similarPipelines = for {
                pCandid <- sortedPipelines.drop(1) if Await.result(sampleEquals(basePipeline._2.lastOutputDataSample, pCandid._2.lastOutputDataSample), Duration(30, TimeUnit.MINUTES))
            } yield pCandid

            val group = Seq(basePipeline) ++ similarPipelines
            groups.append(
                PipelineDataSampleGroup(basePipeline._2.lastOutputDataSample, minIteration(group.map(_._2)), group.toMap)
            )
            sortedPipelines = sortedPipelines.filter(pip => !group.contains(pip))
        }

        groups
    }

    private def sampleEquals(ds1: DataSample, ds2: DataSample)(implicit executionContext: ExecutionContext): Future[Boolean] = {
        val uuid = UUID.randomUUID()
        for {
            m1 <- ds1.getModel(uuid, 0)
            m2 <- ds2.getModel(uuid, 0)
        } yield m1.difference(m2).isEmpty
    }

    def minIteration(pipelines: Seq[Pipeline]): Int = {
        pipelines.map(p => p.lastComponent.discoveryIteration).min
    }
}