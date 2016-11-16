package services.discovery.components.transformer

class Dct_Valid2Time_Interval1Transformer extends SparqlUpdateTransformer {

    protected override val whereClause =
        """
          |  ?s dct:valid ?valid .
          |
          |  BIND(REPLACE(STR(?valid), ".*start=([0-9]{4}-[0-9]{2}-[0-9]{2}).*", "$1") AS ?start1)
          |  BIND(REPLACE(STR(?valid), ".*end=([0-9]{4}-[0-9]{2}-[0-9]{2}).*", "$1") AS ?end1)
          |
          |  BIND(
          |    IF(
          |      CONTAINS(?start1, "end"),
          |      ?end,
          |      ?start1
          |    ) AS ?start
          |  )
          |
          |  BIND(
          |    IF(
          |      CONTAINS(?end1, "start"),
          |      ?start,
          |      ?end1
          |    ) AS ?end
          |  )
          |
          |  OPTIONAL {
          |    ?s dct:title ?title .
          |    BIND(CONCAT("Validity of ", STR(?title)) AS ?abstractionLabel)
          |  }""".stripMargin
    protected override val deleteClause = "?s dct:valid ?valid ."
    protected override val insertClause =
        """
          |?s lpviz:hasAbstraction [
          |    a time:Interval ;
          |    time:hasBeginning [
          |        a time:Instant ;
          |        time:inXSDDateTime ?start
          |    ];
          |    time:hasEnd [
          |        a time:Instant ;
          |        time:inXSDDateTime ?end
          |    ];
          |    rdfs:label ?abstractionLabel
          |] .""".stripMargin
    protected override val prefixes =
        """
          |PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
          |PREFIX dct: <http://purl.org/dc/terms/>
          |PREFIX time: <http://www.w3.org/2006/time#>
          |PREFIX lpviz: <http://visualization.linkedpipes.com/ontology/>
        """.stripMargin
}
