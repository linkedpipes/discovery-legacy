package services.discovery.model.components

import services.discovery.model.Port

trait ExtractorInstance extends AnalyzerInstance

trait SparqlConstructExtractorInstance extends ExtractorInstance with SparqlAnalyzerInstance {
    def port: Port
}
