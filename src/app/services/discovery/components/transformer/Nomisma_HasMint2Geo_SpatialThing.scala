package services.discovery.components.transformer

class Nomisma_HasMint2Geo_SpatialThing extends SparqlUpdateTransformer {

    protected override val prefixes =
        """
          |PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
          |PREFIX geo: <http://www.w3.org/2003/01/geo/wgs84_pos#>
          |PREFIX skos: <http://www.w3.org/2004/02/skos/core#>
          |PREFIX nmo: <http://nomisma.org/ontology#>
          |PREFIX lpviz: <http://visualization.linkedpipes.com/ontology/>
        """.stripMargin

    protected override val deleteClause =
        """
          |?s nmo:hasMint ?mint .
          |?mint geo:location ?location .
        """.stripMargin

    protected override val insertClause =
        """
          |?s lpviz:hasAbstraction ?abstraction .
          |
          |  ?abstraction rdfs:label ?abstractionLabel ;
          |    geo:location ?location .
        """.stripMargin

    protected override val whereClause =
        """
          |?s nmo:hasMint ?mint ;
          |	skos:prefLabel ?label .
          |
          |  ?mint geo:location ?location .
          |
          |  BIND(CONCAT("Location of the mint of ", STR(?label)) AS ?abstractionLabel)
          |
          |  BIND(CONCAT(STR(?s), "/abstraction/nomisma-hasMint2geo-SpatialThing") AS ?abstraction)
          |""".stripMargin
}
