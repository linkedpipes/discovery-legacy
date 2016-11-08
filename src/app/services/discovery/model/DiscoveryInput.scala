package services.discovery.model

import controllers.dto.DiscoverySettings
import services.discovery.components.analyzer.{LinksetBasedUnion, RuianGeocoderAnalyzer}
import services.discovery.components.extractor.{DcatDatasetExtractor, EarthquakesExtractor, PopulatedPlacesExtractor, TownsExtractor}
import services.discovery.components.transformer._
import services.discovery.components.visualizer.{GoogleMapsVisualizer, PopulationVisualizer, TimeVisualizer}
import services.discovery.model.components.{DataSourceInstance, ExtractorInstance, ProcessorInstance, VisualizerInstance}

case class DiscoveryInput(
    dataSources: Seq[DataSourceInstance],
    extractors: Seq[ExtractorInstance],
    visualizers: Seq[VisualizerInstance],
    processors: Seq[ProcessorInstance]
)

object DiscoveryInput {

    val extractors = Seq(
        new TownsExtractor,
        new PopulatedPlacesExtractor,
        new EarthquakesExtractor,
        new DcatDatasetExtractor
    )

    var processors = Seq(
        new LinksetBasedUnion,
        new RuianGeocoderAnalyzer,
        new DbpediaPopulationTransformer,
        new FusionTransformer,
        new Ruian2SchemaOrgTransformer,
        new DbpediaToTimeInstantTransformer,
        new DctermsIssuedToTimeInstantTransformer
    )

    val visualizers = Seq(
        new GoogleMapsVisualizer,
        new TimeVisualizer,
        new PopulationVisualizer
    )

    def create(settings: DiscoverySettings) = {
        new DiscoveryInput(settings.sparqlEndpoints, extractors, visualizers, processors)
    }
}
