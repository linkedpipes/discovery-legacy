package services.discovery.model.components

import services.discovery.model.Port

trait AnalyzerInstance extends ProcessorInstance

trait UnionInstance extends AnalyzerInstance

trait SparqlAnalyzerInstance extends AnalyzerInstance {

  def getQueryByPort(port: Port): SparqlQuery

}
