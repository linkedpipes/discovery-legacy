package services.discovery

import java.util.UUID

import play.Logger
import services.discovery.model.PortCheckResult.Status
import services.discovery.model._
import services.discovery.model.components.{ComponentInstance, ComponentInstanceWithInputs, DataSourceInstance}

import scala.concurrent.{ExecutionContext, Future}

class DiscoveryPortMatcher(discoveryId: UUID, pipelineBuilder: PipelineBuilder)(implicit executor: ExecutionContext) {

    val discoveryLogger = Logger.of("discovery")

    def tryMatchPorts(componentInstance: ComponentInstanceWithInputs, givenPipelines: Seq[Pipeline], iteration: Int): Future[Seq[Pipeline]] = {
        val ports = componentInstance.getInputPorts.sortBy(_.priority)
        discoveryLogger.trace(s"[$discoveryId][$iteration][matcher] Matching ${ports.size} ports against ${givenPipelines.size} pipelines.")
        tryMatchPorts(ports, givenPipelines, portMatches = Map(), lastStates = Seq(None), iteration, componentInstance)
    }

    private def tryMatchPorts(
        remainingPorts: Seq[Port],
        givenPipelines: Seq[Pipeline],
        portMatches: Map[Port, Seq[PortMatch]],
        lastStates: Seq[Option[ComponentState]],
        iterationNumbr: Int,
        componentInstance: ComponentInstanceWithInputs
    ): Future[Seq[Pipeline]] = {
        remainingPorts match {
            case Nil =>
                portMatches.values.forall(_.nonEmpty) match {
                    case true => {
                        discoveryLogger.trace(s"[$discoveryId][$iterationNumbr][matcher] Matching completed, building possible pipelines.")
                        buildPipelines(componentInstance, portMatches, iterationNumbr)
                    }
                    case false => {
                        Future.successful(Seq())
                    }
                }
            case headPort :: tail =>
                val linksets = portMatches.flatMap {
                    pm => pm._2.flatMap {
                        m => m.startPipeline.components.map(_.componentInstance).filter {
                            case c: DataSourceInstance => c.isLinkset
                            case _ => false
                        }
                    }
                }.toSeq.distinct

                tryMatchGivenPipelinesWithPort(headPort, givenPipelines, lastStates, componentInstance, iterationNumbr).flatMap { matches =>
                    tryMatchPorts(tail, givenPipelines, portMatches + (headPort -> matches), matches.map(_.maybeState), iterationNumbr, componentInstance)
                }
        }
    }

    private def tryMatchGivenPipelinesWithPort(
        port: Port,
        givenPipelines: Seq[Pipeline],
        lastStates: Seq[Option[ComponentState]],
        componentInstance: ComponentInstanceWithInputs,
        iterationNumber: Int): Future[Seq[PortMatch]] = {
        val eventualMaybeMatches = Future.sequence {
            for {
                pipeline <- givenPipelines if !pipeline.endsWith(componentInstance)
                state <- lastStates
            } yield {
                val eventualCheckResult = componentInstance.checkPort(port, state, pipeline.lastOutputDataSample, discoveryId, iterationNumber)
                eventualCheckResult.map {
                    case c: PortCheckResult if c.status == Status.Success => Some(PortMatch(port, pipeline, c.maybeState))
                    case c: PortCheckResult if c.status == Status.Error => None
                    case _ => None
                }
            }
        }
        eventualMaybeMatches.map(_.flatten)
    }

    private def buildPipelines(componentInstance: ComponentInstance, portMatches: Map[Port, Seq[PortMatch]], iteration: Int): Future[Seq[Pipeline]] = {
        val allCombinations = combine(portMatches.values)
        discoveryLogger.trace(s"[$discoveryId][$iteration][matcher] ${allCombinations.size} combinations are being processed by PipelineBuilder.")
        Future.sequence(
            allCombinations.map { portSolutions => pipelineBuilder.buildPipeline(componentInstance, portSolutions, iteration) }
        )
    }

    private def combine[A](xs: Traversable[Traversable[A]]): Seq[Seq[A]] = {
        xs.foldLeft(Seq(Seq.empty[A])) {
            (x, y) => for (a <- x.view; b <- y) yield a :+ b
        }
    }
}
