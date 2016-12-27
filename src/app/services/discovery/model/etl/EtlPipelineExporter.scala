package services.discovery.model.etl

import services.discovery.model.Pipeline

object EtlPipelineExporter {

    def export(pipelines: Seq[Pipeline]): Seq[EtlPipeline] = {
        val etlTransformers = pipelines.map(new EtlPipelineTransformer(_))
        val etlPipelines = etlTransformers.map(_.transform)
        val etlSerializers = etlPipelines.map(new EtlPipelineSerializer(_))
        etlSerializers.map { s =>
            val dataset = s.serialize
            EtlPipeline(dataset, s.resultGraphUrn)
        }
    }

}
