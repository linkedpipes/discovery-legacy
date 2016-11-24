package services.discovery.components.transformer

class Movie_Editor2Foaf_Made extends SparqlUpdateTransformer {

    protected override val prefixes =
        """
          |PREFIX foaf: <http://xmlns.com/foaf/0.1/>
          |PREFIX movie: <http://data.linkedmdb.org/resource/movie/>
        """.stripMargin
    protected override val deleteClause =
        """
          |?movie movie:editor ?editor .
        """.stripMargin
    protected override val insertClause =
        """
          |?editor foaf:made ?movie .
        """.stripMargin
    protected override val whereClause =
        """
          |?movie movie:editor ?editor .""".stripMargin
}
