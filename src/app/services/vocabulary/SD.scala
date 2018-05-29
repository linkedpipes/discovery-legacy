package services.vocabulary


object SD extends RdfVocabulary {
  val prefix = "http://www.w3.org/ns/sparql-service-description#"

  val endpoint = property("endpoint")
  val defaultGraph = property("defaultGraph")
}