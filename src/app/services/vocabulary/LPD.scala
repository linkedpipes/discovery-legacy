package services.vocabulary

object LPD extends RdfVocabulary {
  val prefix = "https://discovery.linkedpipes.com/vocabulary/"
  val DataSourceTemplate = resource("DataSourceTemplate")
  val ExtractorTemplate = resource("ExtractorTemplate")
  val TransformerTemplate = resource("TransformerTemplate")
  val ApplicationTemplate = resource("ApplicationTemplate")
  val MandatoryFeature = resource("MandatoryFeature")
  val OptionalFeature = resource("OptionalFeature")
  val Descriptor = resource("Descriptor")

  val componentConfigurationTemplate = property("componentConfigurationTemplate")
  val service = property("service")
  val query = property("query")
  val feature = property("feature")
  val descriptor = property("descriptor")
  val appliesTo = property("appliesTo")
  val outputTemplate = property("outputTemplate")
  val outputDataSample = property("outputDataSample")
  val executor = property("executor")
  val hasTemplate = property("discovery/hasTemplate")
  val hasDiscovery = property("experiment/hasDiscovery")
}