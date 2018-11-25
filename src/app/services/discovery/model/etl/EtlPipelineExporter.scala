package services.discovery.model.etl

import controllers.dto.SparqlEndpointGraph
import services.discovery.model.Pipeline

object EtlPipelineExporter {

    def export(pipeline: Pipeline, endpointGraph: SparqlEndpointGraph): EtlPipeline = {
        val etlTransformer = new EtlPipelineTransformer(pipeline)
        val etlPipeline = etlTransformer.transform
        val etlSerializer = new EtlPipelineSerializer(etlPipeline, endpointGraph)
        val dataset = etlSerializer.serialize
        EtlPipeline(dataset, endpointGraph)
    }

}
