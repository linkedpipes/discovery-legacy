package dao

import java.util.UUID

import scala.concurrent.{ExecutionContext, Future}
import javax.inject.Inject

import models.ExecutionResult
import play.api.db.slick.DatabaseConfigProvider
import play.api.db.slick.HasDatabaseConfigProvider
import slick.jdbc.JdbcProfile

class ExecutionResultDao @Inject() (
    protected val dbConfigProvider: DatabaseConfigProvider
)(implicit executionContext: ExecutionContext) extends HasDatabaseConfigProvider[JdbcProfile] {

    import profile.api._

    private val ExecutionResults = TableQuery[ExecutionResultsTable]

    def all(): Future[Seq[ExecutionResult]] = db.run(ExecutionResults.result)

    def insert(executionResult: ExecutionResult): Future[Unit] = db.run(ExecutionResults += executionResult).map { _ => () }

    private class ExecutionResultsTable(tag: Tag) extends Table[ExecutionResult](tag, "ExecutionResult") {

        def id = column[UUID]("id", O.PrimaryKey)
        def discoveryId = column[String]("discovery_id")
        def pipelineId = column[String]("pipeline_id")
        def graphIri = column[String]("graph_iri")

        def * = (id, discoveryId, pipelineId, graphIri) <> (ExecutionResult.tupled, ExecutionResult.unapply)
    }
}