package services.discovery

import java.util.UUID

import play.Logger
import services.discovery.components.analyzer.LinksetBasedUnion
import services.discovery.model._
import services.discovery.model.components.DataSourceInstance
import services.discovery.model.internal.IterationData

import scala.collection.mutable
import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.ExecutionContext.Implicits.global

class Discovery(val id: UUID, portMatcher: DiscoveryPortMatcher, pipelineBuilder: PipelineBuilder, maximalIterationsCount: Int = 10)(implicit executor: ExecutionContext) {

    private val discoveryLogger = Logger.of("discovery")

    val results = new mutable.HashMap[UUID, Pipeline]

    var isFinished = false

    def discover(input: DiscoveryInput): Future[Seq[Pipeline]] = {
        discoveryLogger.info(s"[$id] Starting with ${input.dataSources.size} datasources, ${input.extractors.size} extractors, ${input.processors.size} processors and ${input.visualizers.size} visualizers.")
        val pipelines = createInitialPipelines(input.dataSources).flatMap { initialPipelines =>
            val data = IterationData(
                id = id,
                givenPipelines = initialPipelines,
                completedPipelines = Seq(),
                availableComponents = input,
                iterationNumber = 1
            )
            iterate(data)
        }

        pipelines.onComplete(_ => isFinished = true)
        pipelines.onSuccess({case p: Seq[Pipeline] => discoveryLogger.info(s"[$id] Success, ${p.size} pipelines found.")})
        pipelines.onFailure({case f => discoveryLogger.info(s"[$id] Failure: ${f.getCause}.\n${f.getStackTrace.mkString("\n")}")})

        pipelines
    }

    private def iterate(iterationData: IterationData): Future[Seq[Pipeline]] = {
        iterationBody(iterationData).flatMap { nextIterationData =>

            val discoveredNewPipeline = nextIterationData.givenPipelines.size > iterationData.givenPipelines.size
            val stop = !discoveredNewPipeline || iterationData.iterationNumber == maximalIterationsCount

            discoveryLogger.debug(s"[$id][${iterationData.iterationNumber}] Iteration finished.")
            discoveryLogger.debug(s"[$id][${iterationData.iterationNumber}] Discovered any new pipelines: $discoveredNewPipeline.")
            discoveryLogger.debug(s"[$id][${iterationData.iterationNumber}] Next iteration: ${!stop}.")

            stop match {
                case true => Future.successful(nextIterationData.completedPipelines)
                case false => iterate(nextIterationData)
            }
        }
    }

    private def endsWithLargeDataset(pipeline: Pipeline): Boolean = {
        pipeline.lastComponent.componentInstance match {
            case ci: DataSourceInstance => ci.isLarge
            case _ => false
        }
    }

    private def iterationBody(iterationData: IterationData): Future[IterationData] = {
        discoveryLogger.debug(s"[$id][${iterationData.iterationNumber}] Starting iteration.")
        discoveryLogger.trace(s"[$id] $iterationData.")

        val (extractorCandidates, otherPipelines) = iterationData.givenPipelines.partition(endsWithLargeDataset)
        val extractors = iterationData.availableComponents.extractors
        val otherComponents = iterationData.availableComponents.processors ++ iterationData.availableComponents.visualizers

        val eventualPipelines = Future.sequence(Seq(
            (extractorCandidates, extractors),
            (otherPipelines, otherComponents)
        ).flatMap {
            case (pipelines, components) => components.map {
                case c if c.isInstanceOf[LinksetBasedUnion] => portMatcher.tryMatchPorts(c, pipelines.filterNot(_.components.exists(_.componentInstance == c)), iterationData.iterationNumber)
                case c => portMatcher.tryMatchPorts(c, pipelines, iterationData.iterationNumber)
            }
        })

        eventualPipelines.map { rawPipelines =>

            val newPipelines = rawPipelines.view.flatten
            val fresh = newPipelines.filter(containsBindingToIteration(iterationData.iterationNumber - 1))
            val (completePipelines, partialPipelines) = fresh.partition(_.isComplete)

            discoveryLogger.debug(s"[$id][${iterationData.iterationNumber}] Found ${newPipelines.size} pipelines, ${fresh.size} were enriched in the last iteration.")
            discoveryLogger.debug(s"[$id][${iterationData.iterationNumber}] ${completePipelines.size} complete pipelines, ${partialPipelines.size} partial.")

            completePipelines.foreach{ p =>
                results.put(UUID.randomUUID(), p)
            }

            val nextIterationGivenPipelines = (iterationData.givenPipelines ++ partialPipelines).distinct
            val nextIterationCompletePipelines = iterationData.completedPipelines ++ completePipelines

            IterationData(
                iterationData.id,
                nextIterationGivenPipelines,
                nextIterationCompletePipelines,
                iterationData.availableComponents,
                iterationData.iterationNumber + 1
            )
        }
    }

    private def createInitialPipelines(dataSources: Seq[DataSourceInstance]): Future[Seq[Pipeline]] = {
        discoveryLogger.trace(s"[$id] Initial pipelines built from $dataSources.")
        Future.sequence(dataSources.map(pipelineBuilder.buildInitialPipeline))
    }

    private def containsBindingToIteration(iterationNumber: Int)(pipeline: Pipeline): Boolean = pipeline.bindings.exists(
        binding => binding.startComponent.discoveryIteration == iterationNumber
    )
}

object Discovery {
    def create : Discovery = {
        val uuid = UUID.randomUUID()
        val builder = new PipelineBuilder(uuid)
        new Discovery(uuid, new DiscoveryPortMatcher(uuid, builder), builder)
    }
}