package services.discovery.model

import controllers.dto.DiscoverySettings
import services.discovery.components.analyzer.{LinksetBasedUnion, RuianGeocoderAnalyzer}
import services.discovery.components.extractor._
import services.discovery.components.transformer._
import services.discovery.components.visualizer._
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
        new DcatDatasetExtractor,
        new LegislationCzActsExtractor,
        new LegislationCzActsVersionsExtractor,
        new LegislationGbActsExtractor,
        new LegislationGbActsVersionsExtractor
    )

    var processors = Seq(
        new LinksetBasedUnion,
        new FusionTransformer,

        new Dbpedia_PopulationTotal2Rdf_ValueTransformer,
        new Dbpedia_Time2Time_InstantTransformer,
        new Dct_Issued2Time_InstantTransformer,
        new Dct_Created2Time_InstantTransformer,
        new Dct_Valid2Time_Interval1Transformer,
        new Dct_Valid2Time_Interval2Transformer,
        new Dct_Date2Time_InstantTransformer,
        new Frbr_Realization2Dct_HasVersionTransformer,
        new Frbr_RealizationOf2Frbr_RealizationTransformer,
        new Ruian_DefinicniBod2Schema_PlaceTransformer,
        new RuianGeocoderAnalyzer,
        new Time_Interval2Time_IntervalTransformer
    )

    val visualizers = Seq(
        new GoogleMapsVisualizer,
        new TemporalEntityVisualizer,
        new TemporalEntityTimeIntervalVisualizer,
        new VersionedTemporalEntityDctermsVersionTimeIntervalVisualizer,
        new PopulationVisualizer
    )

    def create(settings: DiscoverySettings) = {
        new DiscoveryInput(settings.sparqlEndpoints, extractors, visualizers, processors)
    }
}
