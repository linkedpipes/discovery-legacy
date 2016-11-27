package services.discovery.components.transformer

class Wikidata_CoordinateLocation2Geo_SpatialThing extends SparqlUpdateTransformer {

    protected override val prefixes =
        """
          |PREFIX geo: <http://www.w3.org/2003/01/geo/wgs84_pos#>
          |PREFIX dct: <http://purl.org/dc/terms/>
          |PREFIX wdt: <http://www.wikidata.org/prop/direct/>
          |PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
          |PREFIX lpviz: <http://visualization.linkedpipes.com/ontology/>
        """.stripMargin
    protected override val deleteClause =
        """
          |?s wdt:P625 ?coordinates .
        """.stripMargin
    protected override val insertClause =
        """
          |?s lpviz:hasAbstraction [
          |    geo:location [
          |      a geo:SpatialThing ;
          |      geo:long ?long ;
          |      geo:lat ?lat
          |    ]
          |  ] .
          |
          |  ?misto rdfs:label ?abstractionLabel .
        """.stripMargin
    protected override val whereClause =
        """
          |?s wdt:P625 ?coordinates ;
          |    rdfs:label ?label .
          |
          |  BIND(CONCAT("Coordinate location of  ", STR(?label)) AS ?abstractionLabel)
          |
          |  BIND(REPLACE(STR(?coordinates), "Point\\(([0-9]{2}\\.[0-9]*) ([0-9]{2}\\.[0-9]*)\\)", "$1") AS ?lat)
          |
          |  BIND(REPLACE(STR(?coordinates), "Point\\(([0-9]{2}\\.[0-9]*) ([0-9]{2}\\.[0-9]*)\\)", "$2") AS ?long)
          |""".stripMargin
}
