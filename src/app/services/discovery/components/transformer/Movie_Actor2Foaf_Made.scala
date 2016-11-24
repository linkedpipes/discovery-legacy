package services.discovery.components.transformer

class Movie_Actor2Foaf_Made extends SparqlUpdateTransformer {

    protected override val prefixes =
        """
          |PREFIX foaf: <http://xmlns.com/foaf/0.1/>
          |PREFIX movie: <http://data.linkedmdb.org/resource/movie/>
        """.stripMargin
    protected override val deleteClause =
        """
          |?movie movie:actor ?actor .
        """.stripMargin
    protected override val insertClause =
        """
          |?actor foaf:made ?movie .
        """.stripMargin
    protected override val whereClause =
        """
          |?movie movie:actor ?actor .""".stripMargin
}
