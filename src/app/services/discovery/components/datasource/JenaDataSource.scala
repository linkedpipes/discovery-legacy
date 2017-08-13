package services.discovery.components.datasource

import java.util.UUID

import org.apache.jena.rdf.model.Model
import services.discovery.model._
import services.discovery.model.components.DataSourceInstance

import scala.concurrent.Future

class JenaDataSource(override val uri: String, model: Model, val label: String = "", val isLarge: Boolean = false, val isLinkset: Boolean = false) extends DataSourceInstance {
  override def getOutputDataSample(state: Option[ComponentState], dataSamples: Map[Port, DataSample], discoveryId: UUID, iterationNumber: Int): Future[DataSample] = {
    Future.successful(ModelDataSample(model))
  }

  override def toString: String = s"JenaDataSource($label)"
}
