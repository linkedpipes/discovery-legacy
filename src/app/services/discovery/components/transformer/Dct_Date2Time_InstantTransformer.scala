package services.discovery.components.transformer

class Dct_Date2Time_InstantTransformer extends SparqlUpdateTransformer {

    protected override val prefixes =
        """
          |PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
          |PREFIX dct: <http://purl.org/dc/terms/>
          |PREFIX time: <http://www.w3.org/2006/time#>
          |PREFIX lpviz: <http://visualization.linkedpipes.com/ontology/>
        """.stripMargin

    protected override val deleteClause =
        """
          |?s dct:date ?dateTime .
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
          |?s dct:date ?dateTime .
          |
          |OPTIONAL {
          |    ?s dct:title ?title .
          |    BIND(CONCAT("Date of ", STR(?title)) AS ?abstractionLabel)
          |}
          |
          |BIND(IRI(CONCAT(STR(?s), "/abstraction/dct-date2time-Instant")) AS ?abstraction)
        """.stripMargin
}
