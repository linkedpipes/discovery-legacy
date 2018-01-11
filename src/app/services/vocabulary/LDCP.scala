package services.vocabulary

object LDCP extends RdfVocabulary {
  val prefix = "https://linked.opendata.cz/vocabulary/ldcp/"
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
  val hasTemplate = property("hasTemplate")
  val hasExperiments = property("hasExperiments")
}