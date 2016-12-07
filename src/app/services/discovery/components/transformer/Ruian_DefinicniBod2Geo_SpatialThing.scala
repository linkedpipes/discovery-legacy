package services.discovery.components.transformer

class Ruian_DefinicniBod2Geo_SpatialThing extends SparqlUpdateTransformer {

    protected override val prefixes =
        """
          |PREFIX ruian: <http://ruian.linked.opendata.cz/ontology/>
          |PREFIX s: <http://schema.org/>
          |PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
          |PREFIX lpviz: <http://visualization.linkedpipes.com/ontology/>""".stripMargin

    protected override val deleteClause =
        """
          |?s ruian:definicniBod ?bod .
        """.stripMargin

    protected override val insertClause =
        """
          |?s lpviz:hasAbstraction ?bod .
          |
          |?bod rdfs:label ?abstractionLabel .
        """.stripMargin

    protected override val whereClause =
        """
          |?s ruian:definicniBod ?bod .
          |
          |OPTIONAL {
          |    ?s s:name ?label .
          |    BIND(CONCAT("Definition point of ", STR(?label)) AS ?abstractionLabel)
          |}
        """.stripMargin
}
