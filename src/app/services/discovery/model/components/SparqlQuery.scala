package services.discovery.model.components

import org.apache.jena.query.QueryFactory
import org.apache.jena.rdf.model.ModelFactory
import org.topbraid.spin.arq.{ARQ2SPIN, ARQFactory}
import org.topbraid.spin.model._
import org.topbraid.spin.model.update.Modify

trait SparqlQuery {
    def query: String

    def descriptor: AskQuery = {
        val q = QueryFactory.create(query)
        q.setQueryAskType()
        AskQuery(q.serialize())
    }
}

abstract class PlainSparqlQuery extends SparqlQuery

trait SparqlUpdateQuery extends SparqlQuery

trait SparqlAskQuery extends SparqlQuery

trait SparqlConstructQuery extends SparqlQuery

trait SparqlSelectQuery extends SparqlQuery

case class AskQuery(query: String) extends PlainSparqlQuery with SparqlAskQuery

case class SelectQuery(query: String) extends PlainSparqlQuery with SparqlSelectQuery

case class UpdateQuery(query: String) extends PlainSparqlQuery with SparqlUpdateQuery

case class ConstructQuery(query: String) extends PlainSparqlQuery with SparqlConstructQuery