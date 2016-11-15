package services.discovery.components.transformer

class Dct_Issued2Time_InstantTransformer extends SparqlUpdateTransformer {

    protected override val whereClause =
        """
          |?s dct:issued ?dateTime .
          |
          |OPTIONAL {
          |    ?s dct:title ?title .
          |    BIND(CONCAT("Issuance of ", STR(?title)) AS ?abstractionLabel)
          |}""".stripMargin
    protected override val deleteClause = "?s dct:issued ?dateTime ."
    protected override val insertClause =
        """
          |?s lpviz:hasAbstraction [
          |    a time:Instant ;
          |    time:inXSDDateTime ?dateTime ;
          |    rdfs:label ?abstractionLabel
          |] .""".stripMargin
    protected override val prefixes =
        """
          |PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
          |PREFIX dct: <http://purl.org/dc/terms/>
          |PREFIX time: <http://www.w3.org/2006/time#>
          |PREFIX lpviz: <http://visualization.linkedpipes.com/ontology/>""".stripMargin
}
