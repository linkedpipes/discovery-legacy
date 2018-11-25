package services.discovery.model.etl

import controllers.dto.SparqlEndpointGraph
import org.apache.jena.query.Dataset

case class EtlPipeline(dataset: Dataset, resultGraph: SparqlEndpointGraph)
