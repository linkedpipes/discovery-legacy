package services.discovery.components.extractor

class RuianExtractor extends SimpleExtractor {

    override protected val prefixes: String =
        """
          |PREFIX ruian: <http://ruian.linked.opendata.cz/ontology/>
          |PREFIX gml: <http://www.opengis.net/ont/gml#>
          |PREFIX s: <http://schema.org/>
        """.stripMargin

    override protected val constructClause: String =
        """
          |?adresniMisto a ruian:AdresniMisto, s:Place ;
          |    ruian:adresniBod ?adresniBod .
          |
          |  ?adresniBod a gml:Point ;
          |    s:geo ?geoCoordinates .
          |
          |  ?geoCoordinates a s:GeoCoordinates ;
          |    s:longitude ?longitude ;
          |    s:latitude ?latitude .
        """.stripMargin

    override protected val whereClause: String =
        """
          |?adresniMisto a ruian:AdresniMisto, s:Place ;
          |    ruian:adresniBod ?adresniBod .
          |    
          |  ?adresniBod a gml:Point ;
          |    s:geo ?geoCoordinates .
          |    
          |  ?geoCoordinates a s:GeoCoordinates ;
          |    s:longitude ?longitude ;
          |    s:latitude ?latitude .
        """.stripMargin
}
