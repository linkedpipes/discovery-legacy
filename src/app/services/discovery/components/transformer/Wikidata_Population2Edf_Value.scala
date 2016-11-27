package services.discovery.components.transformer

class Wikidata_Population2Edf_Value extends SparqlUpdateTransformer {

    protected override val prefixes =
        """PREFIX wdt: <http://www.wikidata.org/prop/direct/>
          |PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
          |PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
          |PREFIX lpviz: <http://visualization.linkedpipes.com/ontology/>
        """.stripMargin
    protected override val deleteClause =
        """
          |?town wdt:P1082 ?population .
        """.stripMargin
    protected override val insertClause =
        """
          |?town lpviz:hasAbstraction [
          |    rdf:value ?population ;
          |    rdfs:label ?abstractionLabel
          |] .
        """.stripMargin
    protected override val whereClause =
        """
          |?town wdt:P1082 ?population .
          |OPTIONAL {
          |    ?town rdfs:label ?label .
          |    BIND(CONCAT("Population of ", STR(?label)) AS ?abstractionLabel)
          |}
          |""".stripMargin
}
