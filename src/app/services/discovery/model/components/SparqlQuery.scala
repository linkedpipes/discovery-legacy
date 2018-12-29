package services.discovery.model.components

import org.apache.jena.query.{Query, QueryFactory}
import org.apache.jena.update.{UpdateFactory, UpdateRequest}

trait SparqlQuery {
    def queryString : String
}

trait BasicSparqlQuery extends SparqlQuery {
    def query: Query

    lazy val descriptor: AskQuery = toDescriptor

    private def toDescriptor: AskQuery = {
        val q = query.cloneQuery()
        q.setQueryAskType()
        AskQuery(q, q.serialize())
    }
}

case class AskQuery(query: Query, queryString: String) extends BasicSparqlQuery

case class SelectQuery(query: Query, queryString: String) extends BasicSparqlQuery

case class UpdateQuery(updateRequest: UpdateRequest, queryString: String) extends SparqlQuery

case class ConstructQuery(query: Query, queryString: String) extends BasicSparqlQuery

object ConstructQuery {
    def apply(query: String) : ConstructQuery = ConstructQuery(QueryFactory.create(query), query)
}

object AskQuery {
    def apply(query: String) : AskQuery = AskQuery(QueryFactory.create(query), query)
}

object SelectQuery {
    def apply(query: String) : SelectQuery = SelectQuery(QueryFactory.create(query), query)
}

object UpdateQuery {
    def apply(query: String) : UpdateQuery = UpdateQuery(UpdateFactory.create(query), query)
}