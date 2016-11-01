package services.discovery.model.etl

import org.apache.jena.query.Dataset
import services.discovery.model.Pipeline

object EtlPipelineExporter {

  def export(pipelines: Seq[Pipeline]) : Seq[Dataset] = {
    val etlTransformers = pipelines.map(new EtlPipelineTransformer(_))
    val etlPipelines = etlTransformers.map(_.transform)
    val etlSerializers = etlPipelines.map(new EtlPipelineSerializer(_))
    etlSerializers.map(_.serialize)
  }

}
