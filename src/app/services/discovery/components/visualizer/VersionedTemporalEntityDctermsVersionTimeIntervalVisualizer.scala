package services.discovery.components.visualizer

class VersionedTemporalEntityDctermsVersionTimeIntervalVisualizer extends SimpleVisualizer {

    override protected val prefixes: String =
        """
          |PREFIX time: <http://www.w3.org/2006/time#>
          |PREFIX dct: <http://purl.org/dc/terms/>
          |PREFIX lpviz: <http://visualization.linkedpipes.com/ontology/>
          |PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
        """.stripMargin

    override protected val whereClause: String =
        """
          |  [] dct:hasVersion ?version .
          |
          |  ?version lpviz:hasAbstraction ?timeAbstraction .
          |
          |  ?timeAbstraction time:hasBeginning ?beginning ;
          |    time:hasEnd ?end .
          |
          |  OPTIONAL {
          |    ?timeAbstraction rdfs:label ?abstractionLabel .
          |  }
          |
          |  ?beginning time:inXSDDateTime ?dtb .
          |
          |  ?end time:inXSDDateTime ?dte .
        """.stripMargin
}
