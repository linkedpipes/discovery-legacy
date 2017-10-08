package dao

import javax.inject.Inject

import models.DiscoveryResult
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

class DiscoveryResultDao @Inject() (
    protected val dbConfigProvider: DatabaseConfigProvider
)(implicit executionContext: ExecutionContext) extends HasDatabaseConfigProvider[JdbcProfile] {

    import profile.api._

    private val DiscoveryResults = TableQuery[DiscoveryResultsTable]

    def all(): Future[Seq[DiscoveryResult]] = db.run(DiscoveryResults.result)

    def insert(discoveryResult: DiscoveryResult): Future[Unit] = db.run(DiscoveryResults += discoveryResult).map { _ => () }

    def findById(id: String): Future[Option[DiscoveryResult]] = db.run(DiscoveryResults.filter(r => r.id === id).take(1).result.headOption)

    private class DiscoveryResultsTable(tag: Tag) extends Table[DiscoveryResult](tag, "DiscoveryResult") {

        def id = column[String]("id", O.PrimaryKey)
        def data = column[String]("data")

        def * = (id, data) <> (DiscoveryResult.tupled, DiscoveryResult.unapply)
    }
}