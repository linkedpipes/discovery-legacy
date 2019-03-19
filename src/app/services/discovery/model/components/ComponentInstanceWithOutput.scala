package services.discovery.model.components

import java.util.UUID

import services.discovery.model.{ComponentState, DataSample, Port}

import scala.concurrent.Future

trait ComponentInstanceWithOutput extends ComponentInstance {

  def getOutputDataSample(state: Option[ComponentState], dataSamples: Map[Port, DataSample], discoveryId: UUID, iterationNumber: Int): Future[DataSample]

}
