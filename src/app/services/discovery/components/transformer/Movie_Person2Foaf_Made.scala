package services.discovery.components.transformer

class Movie_Person2Foaf_Made extends SparqlUpdateTransformer {

    protected override val prefixes =
        """
          |PREFIX foaf: <http://xmlns.com/foaf/0.1/>
          |PREFIX movie: <http://data.linkedmdb.org/resource/movie/>
        """.stripMargin

    protected override val deleteClause =
        """
          | ?movie movie:actor ?actor ;
          |    movie:editor ?editor ;
          |    movie:music_contributor ?music_contributor ;
          |    movie:producer ?producer ;
          |    movie:writer ?writer .
        """.stripMargin

    protected override val insertClause =
        """
          | ?actor foaf:made ?movie .
          |
          |  ?editor foaf:made ?movie .
          |
          |  ?music_contributor foaf:made ?movie .
          |
          |  ?producer foaf:made ?movie .
          |
          |  ?writer foaf:made ?movie .
        """.stripMargin

    protected override val whereClause =
        """
          | {
          |    ?movie movie:actor ?actor .
          |  } UNION {
          |    ?movie movie:editor ?editor .
          |  } UNION {
          |    ?movie movie:music_contributor ?music_contributor .
          |  } UNION {
          |    ?movie movie:producer ?producer .
          |  } UNION {
          |    ?movie movie:writer ?writer .
          |  }
        """.stripMargin
}
