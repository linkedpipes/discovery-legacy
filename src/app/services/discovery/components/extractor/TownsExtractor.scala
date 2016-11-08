package services.discovery.components.extractor

class TownsExtractor extends SimpleExtractor {

    override protected val prefixes: String =
        """
          |prefix xsd:  <http://www.w3.org/2001/XMLSchema#>
          |prefix rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
          |prefix skos:  <http://www.w3.org/2004/02/skos/core#>
          |prefix s:  <http://schema.org/>
          |prefix ogcgml:  <http://www.opengis.net/ont/gml#>
          |prefix ruian:  <http://ruian.linked.opendata.cz/ontology/>
        """.stripMargin

    override protected val constructClause: String =
        """
          |?obec rdf:type ruian:Obec ;
          |    skos:notation ?notation ;
          |    s:name ?name ;
          |    ruian:definicniBod ?definicniBod ;
          |    ruian:lau ?lau ;
          |    ruian:okres ?okres ;
          |    ruian:pou ?pou .
          |
          |    ?definicniBod rdf:type ogcgml:MultiPoint ;
          |        ogcgml:pointMember ?pointMember .
          |
          |    ?pointMember rdf:type ogcgml:Point ;
          |        ogcgml:pos ?pos ;
          |        s:geo ?geo .
          |    ?geo rdf:type s:GeoCoordinates ;
          |        s:longitude ?lng ;
          |        s:latitude ?lat .
          |
          |    ?pou s:name ?pouname .
        """.stripMargin

    override protected val whereClause: String =
        """
          |?obec rdf:type ruian:Obec ;
          |    skos:notation ?notation ;
          |    s:name ?name ;
          |    ruian:definicniBod ?definicniBod ;
          |    ruian:lau ?lau ;
          |    ruian:okres ?okres ;
          |    ruian:pou ?pou .
          |
          |    ?definicniBod rdf:type ogcgml:MultiPoint ;
          |        ogcgml:pointMember ?pointMember .
          |
          |    ?pointMember rdf:type ogcgml:Point ;
          |        ogcgml:pos ?pos ;
          |        s:geo ?geo .
          |    ?geo rdf:type s:GeoCoordinates ;
          |        s:longitude ?lng ;
          |        s:latitude ?lat .
          |
          |    OPTIONAL { ?pou s:name ?pouname . }
        """.stripMargin
}
