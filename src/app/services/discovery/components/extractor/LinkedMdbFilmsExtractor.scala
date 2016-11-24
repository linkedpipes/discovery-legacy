package services.discovery.components.extractor

class LinkedMdbFilmsExtractor extends SimpleExtractor {

    override protected val prefixes: String =
        """
          |PREFIX movie: <http://data.linkedmdb.org/resource/movie/>
        """.stripMargin

    override protected val constructClause: String =
        """
          |?movie ?p ?o .
        """.stripMargin

    override protected val whereClause: String =
        """
          |  ?movie a movie:film ;
          |    ?p ?o .
        """.stripMargin
}
