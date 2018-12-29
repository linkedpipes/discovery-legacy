package services.discovery.model.components

import services.discovery.model.Port

trait TransformerInstance extends ProcessorInstance

trait SparqlUpdateTransformerInstance extends TransformerInstance {

    def getQueryByPort(port: Port): UpdateQuery

    def transformerGroupIri: Option[String]
}