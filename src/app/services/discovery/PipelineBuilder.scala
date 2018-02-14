package services.discovery

import java.util.UUID
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

import services.discovery.model._
import services.discovery.model.components.{ApplicationInstance, ComponentInstance, ProcessorInstance}

import scala.collection.mutable
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}

class PipelineBuilder(discoveryId: UUID)(implicit executor: ExecutionContext) {
    val pipelineComponentCounter: AtomicInteger = new AtomicInteger()

    def buildPipeline(componentInstance: ComponentInstance, portMatches: Seq[PortMatch], iterationNumber: Int): Future[Pipeline] = {
        val newLastComponent = newComponent(componentInstance, iterationNumber)
        val eventuallyDataSample: Future[DataSample] = dataSample(componentInstance, portMatches, iterationNumber)
        eventuallyDataSample.map { dataSample =>
            Pipeline(
                pipelineComponents(portMatches, newLastComponent),
                pipelineBindings(portMatches, newLastComponent),
                newLastComponent,
                dataSample
            )
        }
    }

    def buildInitialPipeline(dataSet: DataSet): Future[Pipeline] = {
        val dataSourceComponent = newComponent(dataSet.dataSourceInstance, 0)
        val extractorComponent = dataSet.extractorInstance.map(e => newComponent(e, 0))
        dataSet.dataSourceInstance.getOutputDataSample(state = None, dataSamples = Map(), discoveryId, 0).map { outputDataSample =>
            val exSample = dataSet.extractorInstance.map{ ex =>
                val ds = mutable.HashMap[Port, DataSample](ex.getInputPorts.head -> outputDataSample)
                Await.result(ex.getOutputDataSample(None, ds.toMap, discoveryId, 0), Duration(2, TimeUnit.MINUTES))
            }
            Pipeline(
                Seq(Some(dataSourceComponent), extractorComponent).flatten,
                Seq(extractorComponent.map(e => PortBinding(dataSourceComponent, dataSet.extractorInstance.map(_.getInputPorts.head).get, e))).flatten,
                extractorComponent.getOrElse(dataSourceComponent),
                exSample.getOrElse(outputDataSample)
            )
        }
    }

    private def newComponent(componentInstance: ComponentInstance, discoveryIteration: Int): PipelineComponent = {
        PipelineComponent("PC" + pipelineComponentCounter.incrementAndGet(), componentInstance, discoveryIteration)
    }

    private def pipelineComponents(portMatches: Seq[PortMatch], newLastComponent: PipelineComponent): Seq[PipelineComponent] = {
        portMatches.flatMap(_.startPipeline.components ++ Seq(newLastComponent)).distinct
    }

    private def pipelineBindings(portMatches: Seq[PortMatch], newLastComponent: PipelineComponent): Seq[PortBinding] = {
        val newBindings = portMatches.map { portMatch => PortBinding(portMatch.startPipeline.lastComponent, portMatch.port, newLastComponent) }
        portMatches.flatMap(_.startPipeline.bindings) ++ newBindings
    }

    private def dataSample(componentInstance: ComponentInstance, portMatches: Seq[PortMatch], iterationNumber: Int): Future[DataSample] = {
        val dataSamples = portMatches.map { portMatch => portMatch.port -> portMatch.startPipeline.lastOutputDataSample }.toMap
        componentInstance match {
            case c: ProcessorInstance => c.getOutputDataSample(portMatches.last.maybeState, dataSamples, discoveryId, iterationNumber)
            case _: ApplicationInstance => Future.successful(portMatches.head.startPipeline.lastOutputDataSample)
        }
    }
}
