package services.discovery.model.components

trait SparqlQuery {
  def query: String
}

case class AskQuery(query: String) extends SparqlQuery

case class SelectQuery(query: String) extends SparqlQuery

case class UpdateQuery(query: String) extends SparqlQuery

case class ConstructQuery(query: String) extends SparqlQuery