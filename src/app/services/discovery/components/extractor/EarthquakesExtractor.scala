package services.discovery.components.extractor

class EarthquakesExtractor extends SimpleExtractor {

    override protected val prefixes: String =
        """
          |PREFIX dbo: <http://dbpedia.org/ontology/>
          |PREFIX dbp: <http://dbpedia.org/property/>""".stripMargin

    override protected val constructClause: String =
        """
          |?e a dbo:Earthquake ;
          |    ?p ?o .
        """.stripMargin

    override protected val whereClause: String =
        """
          |?e a dbo:Earthquake ;
          |    ?p ?o .
        """.stripMargin
}
