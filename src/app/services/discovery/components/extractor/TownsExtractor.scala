package services.discovery.components.extractor

class TownsExtractor extends SimpleExtractor {

    override protected val prefixes: String =
        """
          |PREFIX ruian: <http://ruian.linked.opendata.cz/ontology/>
          |PREFIX gml: <http://www.opengis.net/ont/gml#>
          |PREFIX s: <http://schema.org/>
        """.stripMargin

    override protected val constructClause: String =
        """
          |  ?obec a ruian:Obec ;
          |    ruian:definicniBod ?definicniBod ;
          |    s:name ?nazevObce .
          |
          |  ?definicniBod a gml:Point ;
          |    s:geo ?geoCoordinates .
          |
          |  ?geoCoordinates a s:GeoCoordinates ;
          |    s:longitude ?longitude ;
          |    s:latitude ?latitude .
        """.stripMargin

    override protected val whereClause: String =
        """
          |?obec a ruian:Obec ;
          |    ruian:definicniBod ?definicniBod ;
          |    s:name ?nazevObce .
          |
          |  ?definicniBod a gml:Point ;
          |    s:geo ?geoCoordinates .
          |
          |  ?geoCoordinates a s:GeoCoordinates ;
          |    s:longitude ?longitude ;
          |    s:latitude ?latitude .
        """.stripMargin
}
