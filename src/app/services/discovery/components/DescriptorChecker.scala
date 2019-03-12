package services.discovery.components

import java.util.UUID

import services.discovery.model.PortCheckResult.Status
import services.discovery.model.components.{AskQuery, SparqlQuery}
import services.discovery.model.{DataSample, PortCheckResult}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait DescriptorChecker {
  private val errorCheckResult: Future[PortCheckResult] = Future.successful(PortCheckResult(Status.Error))

  protected def checkStatelessDescriptors(dataSample: DataSample, discoveryId: UUID, iterationNumber: Int, descriptors: SparqlQuery*): Future[PortCheckResult] = {

    val eventuallyDescriptorChecks = Future.sequence(descriptors.map {
      case d: AskQuery => dataSample.executeAsk(d)
      case some => throw new RuntimeException("Unsupported type of descriptor: " + some)
    })


    val eventuallyPortCheckResult: Future[PortCheckResult] = eventuallyDescriptorChecks.map { descriptorCheckResults =>
      PortCheckResult(
        descriptorCheckResults.forall(identity),
        None
      )
    }

    eventuallyPortCheckResult.fallbackTo(errorCheckResult)
  }
}
