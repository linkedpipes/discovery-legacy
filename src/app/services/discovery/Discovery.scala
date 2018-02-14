package services.discovery

import java.util.UUID

import play.Logger
import services.discovery.components.analyzer.LinksetBasedUnion
import services.discovery.components.transformer.FusionTransformer
import services.discovery.model._
import services.discovery.model.components.{DataSourceInstance, ExtractorInstance, SparqlUpdateTransformerInstance}
import services.discovery.model.internal.IterationData

import scala.collection.mutable
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._


class Discovery(val id: UUID, val input: DiscoveryInput, maximalIterationsCount: Int = 10)
               (portMatcher: DiscoveryPortMatcher, pipelineBuilder: PipelineBuilder)
               (implicit executor: ExecutionContext)
{

    val results = new mutable.HashMap[UUID, Pipeline]

    var isFinished = false

    private val discoveryLogger = Logger.of("discovery")

    private val startTime = System.nanoTime()

    private var endTime : Long = 0

    def duration: Long = {
        (endTime - startTime) / (1000 * 1000) // ns -> ms
    }

    def start: Future[Seq[Pipeline]] = {
        discoveryLogger.info(s"[$id] Starting with ${input.dataSets.size} data sets, ${input.processors.size} processors and ${input.applications.size} applications.")
        val pipelines = createInitialPipelines(input.dataSets).flatMap { initialPipelines =>
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

            val discoveredNewPipeline = nextIterationData.givenPipelines.lengthCompare(iterationData.givenPipelines.size) > 0
            val stop = !discoveredNewPipeline || iterationData.iterationNumber == maximalIterationsCount

            discoveryLogger.debug(s"[$id][${iterationData.iterationNumber}] Iteration finished.")
            discoveryLogger.debug(s"[$id][${iterationData.iterationNumber}] Discovered any new pipelines: $discoveredNewPipeline.")
            discoveryLogger.debug(s"[$id][${iterationData.iterationNumber}] Next iteration: ${!stop}.")

            stop match {
                case true => {
                    endTime = System.nanoTime()
                    Future.successful(nextIterationData.completedPipelines)
                }
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

        val (extractorCandidatePipelines, otherPipelines) = iterationData.givenPipelines.partition(endsWithLargeDataset)
        val extractors = iterationData.availableComponents.extractors
        val otherComponents = iterationData.availableComponents.processors ++ iterationData.availableComponents.applications

        val eventualPipelines = Future.sequence(Seq(
            (extractorCandidatePipelines, extractors),
            (otherPipelines, otherComponents)
        ).flatMap {
            case (pipelines, components) => {

                components.map { component =>

                    val filteredPipelines = component match {
                        case c if c.isInstanceOf[LinksetBasedUnion] => pipelines.filterNot(_.components.count(_.componentInstance == c) > 0)
                          .filterNot(p => p.components.lengthCompare(1) == 0 && p.lastComponent.componentInstance.isInstanceOf[DataSourceInstance] &&
                            p.lastComponent.componentInstance.asInstanceOf[DataSourceInstance].isLarge)
                        case c if c.isInstanceOf[FusionTransformer] => pipelines.filter(_.components.exists(_.componentInstance.isInstanceOf[LinksetBasedUnion]))
                        case e if e.isInstanceOf[ExtractorInstance] => pipelines.filter(_.lastComponent.componentInstance.isInstanceOf[DataSourceInstance])
                        case _ => pipelines
                    }

                    portMatcher.tryMatchPorts(component, filteredPipelines, iterationData.iterationNumber)
                }
            }
        })

        eventualPipelines.map { rawPipelines =>

            val newPipelines = rawPipelines.view.flatten
            val fresh = newPipelines.filter(containsBindingToIteration(iterationData.iterationNumber - 1))
            val (completePipelines, pipelineFragments) = fresh.partition(_.isComplete)

            discoveryLogger.debug(s"[$id][${iterationData.iterationNumber}] Found ${newPipelines.size} pipelines, ${fresh.size} were enriched in the last iteration.")
            discoveryLogger.debug(s"[$id][${iterationData.iterationNumber}] ${completePipelines.size} complete pipelines, ${pipelineFragments.size} pipeline fragments.")

            val consolidatedFragments = consolidateFragments(pipelineFragments.toSeq, iterationData.iterationNumber)

            completePipelines.foreach{ p =>
                results.put(UUID.randomUUID(), p)
            }

            val nextIterationCompletePipelines = iterationData.completedPipelines ++ completePipelines
            val nextIterationGivenPipelines = input.needsSmallerFragments match {
                case true => (iterationData.givenPipelines ++ consolidatedFragments).distinct
                case false => consolidatedFragments
            }

            IterationData(
                iterationData.id,
                nextIterationGivenPipelines,
                nextIterationCompletePipelines,
                iterationData.availableComponents,
                iterationData.iterationNumber + 1
            )
        }
    }

    private def consolidateFragments(pipelineFragments: Seq[Pipeline], discoveryIteration: Int) : Seq[Pipeline] = {
        val (endingWithTransformer, others) = pipelineFragments.partition(p => p.lastComponent.componentInstance.isInstanceOf[SparqlUpdateTransformerInstance])
        val transformerGroups = endingWithTransformer.groupBy(p => p.lastComponent.componentInstance.asInstanceOf[SparqlUpdateTransformerInstance].transformerGroupIri)
        val (withTransformerGroup, withoutTransformerGroup) = transformerGroups.partition(tg => tg._1.isDefined)

        val newFragments = withTransformerGroup.map { transformerGroup =>
            val distinctTransformers = transformerGroup._2.map(_.lastComponent.componentInstance.asInstanceOf[SparqlUpdateTransformerInstance]).distinct
            val randomPipelineFragment = transformerGroup._2.head
            val transformer = randomPipelineFragment.lastComponent.componentInstance.asInstanceOf[SparqlUpdateTransformerInstance]
            val transformersToAttach = distinctTransformers.filter(t => t != transformer)

            var fragment = randomPipelineFragment
            transformersToAttach.foreach { t =>
                fragment = Await.ready(pipelineBuilder.buildPipeline(t, Seq(PortMatch(t.getInputPorts.head, fragment, None)), discoveryIteration), atMost = Duration(30, SECONDS)).value.get.get
            }
            fragment
        }

        others ++ withoutTransformerGroup.values.flatten ++ newFragments
    }

    private def createInitialPipelines(dataSets: Seq[DataSet]): Future[Seq[Pipeline]] = {
        discoveryLogger.trace(s"[$id] Initial pipelines built from $dataSets.")
        Future.sequence(dataSets.map(pipelineBuilder.buildInitialPipeline))
    }

    private def containsBindingToIteration(iterationNumber: Int)(pipeline: Pipeline): Boolean = pipeline.bindings.exists(
        binding => binding.startComponent.discoveryIteration == iterationNumber
    )
}

object Discovery {
    def create(input: DiscoveryInput) : Discovery = {
        val uuid = UUID.randomUUID()
        val builder = new PipelineBuilder(uuid)
        new Discovery(uuid, input)(new DiscoveryPortMatcher(uuid, builder), builder)
    }
}