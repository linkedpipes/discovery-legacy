package services.discovery.components.transformer

class Dct_Created2Time_InstantTransformer extends SparqlUpdateTransformer {

    protected override val prefixes =
        """
          |PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
          |PREFIX dct: <http://purl.org/dc/terms/>
          |PREFIX time: <http://www.w3.org/2006/time#>
          |PREFIX lpviz: <http://visualization.linkedpipes.com/ontology/>
        """.stripMargin

    protected override val deleteClause =
        """
          |?s dct:created ?dateTime .
        """.stripMargin

    protected override val insertClause =
        """
          |?s lpviz:hasAbstraction ?abstraction .
          |
          |?abstraction a time:Instant ;
          |    time:inXSDDateTime ?dateTime ;
          |    rdfs:label ?abstractionLabel .
        """.stripMargin

    protected override val whereClause =
        """
          |?s dct:created ?dateTime .
          |
          |OPTIONAL {
          |    ?s dct:title ?title .
          |    BIND(CONCAT("Creation of ", STR(?title)) AS ?abstractionLabel)
          |}
          |
          |BIND(IRI(CONCAT(STR(?s), "/abstraction/dbp-time2time-Instant")) AS ?abstraction)
        """.stripMargin
}
