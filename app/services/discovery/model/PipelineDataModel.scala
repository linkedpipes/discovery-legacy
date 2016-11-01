package services.discovery.model

import org.apache.jena.query.{Dataset, DatasetFactory}
import org.apache.jena.rdf.model.{Model, ModelFactory}

case class PipelineDataModel(dataset: Dataset, pipelineModel: Model)

object PipelineDataModel {
  def create(pipelineUrn: String) = {
    val dataset = DatasetFactory.createMem()
    val pipelineJenaModel = ModelFactory.createDefaultModel()
    dataset.addNamedModel(pipelineUrn, pipelineJenaModel)
    PipelineDataModel(dataset, pipelineJenaModel)
  }
}