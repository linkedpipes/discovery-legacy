package services.discovery.model.components

import org.apache.jena.query.{Query, QueryFactory}

trait SparqlQuery {
    def query: Query

    lazy val descriptor: AskQuery = toDescriptor

    private def toDescriptor: AskQuery = {
        val q = query.cloneQuery()
        q.setQueryAskType()
        AskQuery(q)
    }
}

object ConstructQuery {
    def apply(query: String) : ConstructQuery = ConstructQuery(QueryFactory.create(query))
}

object AskQuery {
    def apply(query: String) : AskQuery = AskQuery(QueryFactory.create(query))
}

object SelectQuery {
    def apply(query: String) : SelectQuery = SelectQuery(QueryFactory.create(query))
}

object UpdateQuery {
    def apply(query: String) : UpdateQuery = UpdateQuery(QueryFactory.create(query))
}

abstract class PlainSparqlQuery extends SparqlQuery

trait SparqlUpdateQuery extends SparqlQuery

trait SparqlAskQuery extends SparqlQuery

trait SparqlConstructQuery extends SparqlQuery

trait SparqlSelectQuery extends SparqlQuery

case class AskQuery(query: Query) extends PlainSparqlQuery with SparqlAskQuery

case class SelectQuery(query: Query) extends PlainSparqlQuery with SparqlSelectQuery

case class UpdateQuery(query: Query) extends PlainSparqlQuery with SparqlUpdateQuery

case class ConstructQuery(query: Query) extends PlainSparqlQuery with SparqlConstructQuery