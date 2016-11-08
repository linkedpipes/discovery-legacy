package services.discovery.components.extractor

class PopulatedPlacesExtractor extends SimpleExtractor {

    override protected val prefixes: String =
        """
          |PREFIX dbo: <http://dbpedia.org/ontology/>
          |PREFIX dbp: <http://dbpedia.org/property/>
        """.stripMargin

    override protected val constructClause: String =
        """
          |?p a dbo:PopulatedPlace ;
          |    dbo:populationTotal ?population ;
          |    dbp:officialName ?on .
        """.stripMargin

    override protected val whereClause: String =
        """
          |?p a dbo:PopulatedPlace ;
          |    dbo:populationTotal ?population ;
          |    dbp:officialName ?on .
        """.stripMargin
}
