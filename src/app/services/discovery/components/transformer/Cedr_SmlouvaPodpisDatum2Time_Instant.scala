package services.discovery.components.transformer

class Cedr_SmlouvaPodpisDatum2Time_Instant extends SparqlUpdateTransformer {

    protected override val whereClause =
        """
          |?s cedr:smlouvaPodpisDatum ?dateTime .
          |
          |OPTIONAL {
          |    ?s dct:title ?title .
          |    BIND(CONCAT("Signature of agreement number ", STR(?title)) AS ?abstractionLabel)
          |}
        """.stripMargin
    protected override val deleteClause = """?s cedr:smlouvaPodpisDatum ?dateTime ."""
    protected override val insertClause =
        """
          |?s lpviz:hasAbstraction [
          |    a time:Instant ;
          |    time:inXSDDateTime ?dateTime ;
          |    rdfs:label ?abstractionLabel
          |] .
        """.stripMargin
    protected override val prefixes =
        """
          |PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
          |PREFIX cedr: <http://cedropendata.mfcr.cz/c3lod/cedr/vocabCEDR#>
          |PREFIX dct: <http://purl.org/dc/terms/>
          |PREFIX time: <http://www.w3.org/2006/time#>
          |PREFIX lpviz: <http://visualization.linkedpipes.com/ontology/>""".stripMargin
}
