package services.discovery.components.transformer

class Movie_PersonName2Foaf_Name extends SparqlUpdateTransformer {

    protected override val prefixes =
        """
          |PREFIX foaf: <http://xmlns.com/foaf/0.1/>
          |PREFIX movie: <http://data.linkedmdb.org/resource/movie/>
        """.stripMargin

    protected override val deleteClause =
        """
          |?person movie:music_contributor_name ?music_contributor_name ;
          |    movie:producer_name ?producer_name ;
          |	movie:writer_name ?writer_name .
        """.stripMargin

    protected override val insertClause =
        """
          |?person a foaf:Person ;
          |	foaf:name ?music_contributor_name, ?producer_name, ?writer_name .
        """.stripMargin

    protected override val whereClause =
        """
          |{
          |    ?person movie:music_contributor_name ?music_contributor_name .
          |  } UNION {
          |    ?person movie:producer_name ?producer_name .
          |  } UNION {
          |    ?person movie:writer_name ?writer_name .
          |  }
          |
        """.stripMargin
}
