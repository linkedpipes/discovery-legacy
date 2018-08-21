package services.discovery.model.etl

import org.apache.jena.query.Dataset

case class EtlPipeline(dataset: Dataset, sparqlEndpointIri: String, resultGraphIri: String)
