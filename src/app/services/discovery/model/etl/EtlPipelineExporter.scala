package services.discovery.model.etl

import services.discovery.model.Pipeline

object EtlPipelineExporter {

    def export(pipeline: Pipeline, endpointUri: String): EtlPipeline = {
        val etlTransformer = new EtlPipelineTransformer(pipeline)
        val etlPipeline = etlTransformer.transform
        val etlSerializer = new EtlPipelineSerializer(etlPipeline, endpointUri)
        val dataset = etlSerializer.serialize
        EtlPipeline(dataset, etlSerializer.resultGraphIri)
    }

}
