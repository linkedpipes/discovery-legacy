package services.discovery.components.transformer

class Movie_MusicContributor2Foaf_Made extends SparqlUpdateTransformer {

    protected override val prefixes =
        """
          |PREFIX foaf: <http://xmlns.com/foaf/0.1/>
          |PREFIX movie: <http://data.linkedmdb.org/resource/movie/>
        """.stripMargin
    protected override val deleteClause =
        """
          |?movie movie:music_contributor ?music_contributor .
        """.stripMargin
    protected override val insertClause =
        """
          |?music_contributor foaf:made ?movie .
        """.stripMargin
    protected override val whereClause =
        """
          |?movie movie:music_contributor ?music_contributor .""".stripMargin
}
