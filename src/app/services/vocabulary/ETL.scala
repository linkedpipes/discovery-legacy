package services.vocabulary

object ETL extends RdfVocabulary {
  val prefix = "http://etl.linkedpipes.com/ontology/"

  val executionStatus = property("executionStatus")
}
