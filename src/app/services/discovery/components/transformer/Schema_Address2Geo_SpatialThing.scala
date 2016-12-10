package services.discovery.components.transformer

class Schema_Address2Geo_SpatialThing extends SparqlUpdateTransformer {

    protected override val prefixes =
        """
          |PREFIX dct: <http://purl.org/dc/terms/>
          |PREFIX s: <http://schema.org/>
          |PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
          |PREFIX lpviz: <http://visualization.linkedpipes.com/ontology/>
        """.stripMargin

    protected override val deleteClause =
        """
          |?s s:address ?address .
        """.stripMargin

    protected override val insertClause =
        """
          |?s lpviz:hasAbstraction ?address .
          |
          |  ?address rdfs:label ?abstractionLabel .
        """.stripMargin

    protected override val whereClause =
        """
          |?s s:address ?address ;
          |    dct:title ?title .
          |
          |  BIND(CONCAT("Address of ", STR(?title)) AS ?abstractionLabel)
        """.stripMargin
}
