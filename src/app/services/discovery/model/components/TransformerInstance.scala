package services.discovery.model.components

import services.discovery.model.Port

trait TransformerInstance extends ProcessorInstance

trait SparqlTransformerInstance extends TransformerInstance {

  def getQueryByPort(port: Port) : SparqlQuery

}