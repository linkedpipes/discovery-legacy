package services.discovery.components.transformer

class Time_Interval2Time_IntervalTransformer extends SparqlUpdateTransformer {

    protected override val prefixes =
        """
          |PREFIX time: <http://www.w3.org/2006/time#>
          |PREFIX lpviz: <http://visualization.linkedpipes.com/ontology/>
        """.stripMargin
    protected override val deleteClause = "?s ?p ?interval ."
    protected override val insertClause =
        """
          |?s lpviz:hasAbstraction ?interval .
        """.stripMargin
    protected override val whereClause =
        """
          |?s ?p ?interval .
          |FILTER (?p != lpviz:hasAbstraction)
          |
          |{
          |    ?interval time:hasBeginning ?beginning .
          |} UNION {
          |    ?interval time:hasEnd ?end .
          |}""".stripMargin
}
