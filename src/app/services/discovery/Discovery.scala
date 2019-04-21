package services.discovery

import java.util.UUID

import play.Logger
import services.discovery.components.analyzer.LinksetBasedUnion
import services.discovery.components.transformer.FusionTransformer
import services.discovery.model._
import services.discovery.model.components.{ComponentInstanceWithInputs, ExtractorInstance}
import services.discovery.model.internal.{DiscoveryIteration, FragmentList}

import scala.collection.mutable
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._


class Discovery(val id: UUID, val input: DiscoveryInput, maxIterations: Int = 10, preserveFragments : Boolean = false)
   (portMatcher: DiscoveryPortMatcher, pipelineBuilder: PipelineBuilder)
   (implicit executor: ExecutionContext)
{

    val results = new mutable.HashMap[UUID, Pipeline]

    var isFinished = false

    val onStop : mutable.ArrayBuffer[Discovery => Unit] = new mutable.ArrayBuffer[Discovery => Unit]()

    val timer = new Timer

    private val discoveryLogger = Logger.of("discovery")

    def start: Future[Seq[Pipeline]] = {
        timer.start
        discoveryLogger.info(s"[$id] Starting with ${input.dataSets.size} data sets, ${input.processors.size} processors and ${input.applications.size} applications.")

        val initialFragments = createInitialPipelineFragments(input.dataSets)
        val pipelines = initialFragments.flatMap { fragments =>
            val iteration = DiscoveryIteration(
                id = id,
                fragments = fragments,
                pipelines = Seq(),
                input = input,
                number = 1
            )
            iterate(iteration)
        }

        pipelines.onComplete(_ => isFinished = true)
        pipelines.onSuccess({case p: Seq[Pipeline] => discoveryLogger.info(s"[$id] Success, ${p.size} pipelines found.")})
        pipelines.onFailure({case f => discoveryLogger.info(s"[$id] Failure: ${f.getCause}.\n${f.getStackTrace.mkString("\n")}")})

        pipelines
    }

    private def iterate(iteration: DiscoveryIteration): Future[Seq[Pipeline]] = {
        iterationBody(iteration).flatMap { nextIteration =>

            val discoveredNewPipeline = nextIteration.fragments.fresh.nonEmpty
            val stop = !discoveredNewPipeline || iteration.number == maxIterations

            discoveryLogger.info(s"[$id][${iteration.number}] Iteration finished.")
            discoveryLogger.debug(s"[$id][${iteration.number}] Discovered any new pipelines: $discoveredNewPipeline.")
            discoveryLogger.debug(s"[$id][${iteration.number}] Next iteration: ${!stop}.")

            stop match {
                case true => finalize(nextIteration)
                case false => iterate(nextIteration)
            }
        }
    }

    private def iterationBody(iteration: DiscoveryIteration): Future[DiscoveryIteration] = {
        discoveryLogger.info(s"[$id][${iteration.number}] Starting iteration.")

        val eventualPipelines = Future.sequence(
            getCombinatorInput(iteration).flatMap { ci =>
                ci.components.map { component =>
                    val relevantFragments = getRelevantFragments(component, ci.fragments)
                    portMatcher.tryMatchPorts(component, relevantFragments, iteration.number)
                }
            }
        )

        eventualPipelines.map { rawPipelines =>

            val newPipelines = rawPipelines.flatten
            val fresh = preserveFragments match {
                case true => newPipelines.filter(containsBindingToIteration(iteration.number - 1))
                case false => newPipelines
            }
            val (completePipelines, pipelineFragments) = fresh.partition(_.isComplete)

            discoveryLogger.info(s"[$id][${iteration.number}] Found ${newPipelines.size} pipelines in the last iteration, ${fresh.size} new.")
            discoveryLogger.info(s"[$id][${iteration.number}] ${completePipelines.size} complete pipelines, ${pipelineFragments.size} pipeline fragments.")

            val consolidatedFragments = consolidateFragments(pipelineFragments, iteration.number)

            completePipelines.foreach{ p =>
                results.put(UUID.randomUUID(), p)
            }

            val nextFragmentList = preserveFragments match {
                case true => FragmentList(iteration.fragments.ordinary, consolidatedFragments.diff(iteration.fragments.ordinary).seq, Seq())
                case false => FragmentList(Seq(), consolidatedFragments, Seq())
            }

            DiscoveryIteration(
                id = iteration.id,
                fragments = nextFragmentList,
                pipelines = iteration.pipelines ++ completePipelines,
                input = iteration.input,
                number = iteration.number + 1
            )
        }
    }

    private def consolidateFragments(fragments: Seq[Pipeline], iterationNumber: Int) : Seq[Pipeline] = {
        val (endingWithTransformer, others) = fragments.partition(p => p.endsWithSparqlUpdateTransformerInstance)
        val datasourceGroups = endingWithTransformer.groupBy(p => p.typedDatasources)
        val newDatasourceGroupFragments = datasourceGroups.map { case (_, datasourceGroupFragments) =>
            val transformerGroups = datasourceGroupFragments.groupBy(p => p.endingTransformerGroupIri)
            val (withTransformerGroup, withoutTransformerGroup) = transformerGroups.partition(tg => tg._1.isDefined)
            val newFragments = applyTransformerGroup(withTransformerGroup, iterationNumber)
            withoutTransformerGroup.values.flatten ++ newFragments
        }

        others ++ newDatasourceGroupFragments.flatten
    }

    private def applyTransformerGroup(fragmentMap: Map[Option[String], Seq[Pipeline]], iterationNumber: Int) = {
        fragmentMap.map { case (_, fragments) =>
            val basePipelineFragment = fragments.head
            val distinctTransformers = fragments.drop(1).map(_.endingTransformer).distinct.filterNot(t => t == basePipelineFragment.endingTransformer)

            var fragment = basePipelineFragment
            distinctTransformers.foreach { t =>
                fragment = Await.ready(
                    pipelineBuilder.buildPipeline(t, Seq(PortMatch(t.getInputPorts.head, fragment, None)), iterationNumber), atMost = Duration(30, SECONDS)
                ).value.get.get
            }
            fragment
        }
    }

    private def finalize(iterationData: DiscoveryIteration): Future[Seq[Pipeline]] = {
        timer.stop
        onStop.foreach(f => f(this))
        Future.successful(iterationData.pipelines)
    }

    private def getCombinatorInput(iteration: DiscoveryIteration) : Seq[CombinatorInput] = {
        iteration.fragments.largeDatasets.isEmpty match {
            case true => Seq(
                CombinatorInput(iteration.fragments.ordinary, iteration.input.processors ++ iteration.input.applications)
            )
            case false => Seq(
                CombinatorInput(iteration.fragments.largeDatasets, iteration.input.extractors),
                CombinatorInput(iteration.fragments.ordinary, iteration.input.processors ++ iteration.input.applications)
            )
        }
    }

    private def getRelevantFragments(component: ComponentInstanceWithInputs, fragments: Seq[Pipeline]) : Seq[Pipeline] = {
        component match {
            case c if c.isInstanceOf[LinksetBasedUnion] => fragments.filterNot(p => p.containsInstance(c) || p.endsWithLargeDataset).seq
            case c if c.isInstanceOf[FusionTransformer] => fragments.filter(_.containsLinksetBasedUnion).seq
            case e if e.isInstanceOf[ExtractorInstance] => fragments.filter(_.endsWithDataSourceInstance).seq
            case _ => fragments
        }
    }

    private def createInitialPipelineFragments(dataSets: Seq[DataSet]): Future[FragmentList] = {
        discoveryLogger.trace(s"[$id] Initial pipelines built from $dataSets.")
        val pipelines = Future.sequence(dataSets.map(pipelineBuilder.buildInitialPipeline))
        pipelines.map { p =>
            val (l, f) = p.partition(_.endsWithLargeDataset)
            FragmentList(Seq(), f, l)
        }
    }

    private def containsBindingToIteration(iterationNumber: Int)(pipeline: Pipeline): Boolean = {
        pipeline.bindings.exists(
            binding => binding.startComponent.discoveryIteration == iterationNumber
        )
    }
}

object Discovery {
    def create(input: DiscoveryInput) : Discovery = {
        val uuid = UUID.randomUUID()
        val builder = new PipelineBuilder(uuid)
        new Discovery(uuid, input)(new DiscoveryPortMatcher(uuid, builder), builder)
    }
}