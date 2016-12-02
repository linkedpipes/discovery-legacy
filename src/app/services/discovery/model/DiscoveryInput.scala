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
        new PopulatedPlacesExtractor,
        new EarthquakesExtractor,
        new DcatDatasetExtractor,
        new LegislationCzActsExtractor,
        new LegislationCzActsVersionsExtractor,
        new LegislationGbActsExtractor,
        new LegislationGbActsVersionsExtractor,
        new SubsidiesCzCedrExtractor,
        new NomismaOrgPersonsExtractor,
        new LinkedMdbFilmsExtractor,
        new AresExtractor,
        new RuianExtractor,
        new TownsExtractor,
        new WikidataTownsExtractor
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
        new Time_Interval2Time_IntervalTransformer,
        new Cedr_DotaceCastka2Rdf_Value,
        new Cedr_SidliNaAdrese2Geo_SpatialThing,
        new Cedr_SmlouvaPodpisDatum2Dct_Created,
        new Cedr_SmlouvaPodpisDatum2Time_Instant,
        new Foaf_Maker2Foaf_Made,
        new Foaf_Name2Dct_Title,
        new Foaf_RdfsLabel2Foaf_Name,
        new Foaf_SkosPrefLabel2Foaf_Name,
        new Gr_LegalName2Dct_Title,
        new LinkedMdb_InitialReleaseOf2Time_Instant,
        new Movie_Actor2Foaf_Made,
        new Movie_Editor2Foaf_Made,
        new Movie_MusicContributorName2Foaf_Name,
        new Movie_MusicContributorName2Foaf_Made,
        new Movie_ProducerName2Foaf_Name,
        new Movie_Producer2Foaf_Made,
        new Movie_WriterName2Foaf_Name,
        new Movie_Writer2Foaf_Made,
        new Nomisma_HasAuthority2Foaf_Made,
        new Nomisma_HasMint2Geo_SpatialThing,
        new Nomisma_StartDateEndDate2Time_Interval,
        new Org_HasMembership2Org_Member,
        new Ruian_AdresniMisto2Geo_SpatialThing,
        new Schema_GeoCoordinates2Geo_SpatialThing,
        new Swrc_Editor2Foaf_Made,
        new Wikidata_Population2Edf_Value,
        new Wikidata_CoordinateLocation2Geo_SpatialThing,
        new Ruian_DefinicniBod2Geo_SpatialThing
    )

    val visualizers = Seq(
        new TemporalEntityVisualizer,
        new TemporalEntityTimeIntervalVisualizer,
        new VersionedTemporalEntityDctermsVersionTimeIntervalVisualizer,
        new PopulationVisualizer,
        new SpatialThingVisualizer,
        new PersonalProfilesVisualizer,
        new ThingsOnMapVisualizer
    )

    def create(settings: DiscoverySettings) = {
        new DiscoveryInput(settings.sparqlEndpoints, extractors, visualizers, processors)
    }
}
