package services.discovery.components.visualizer

class VersionedTemporalEntityDctermsVersionTimeIntervalVisualizer extends SimpleVisualizer {

    override protected val prefixes: String =
        """
          |  [] dct:hasVersion ?version ;
          |
          |  ?version lpviz:hasAbstraction ?timeAbstraction ;
          |
          |  ?timeAbstraction time:hasBeginning ?beginning ;
          |    time:hasEnd ?end .
          |
          |  ?beginning time:inXSDDateTime ?dtb .
          |
          |  ?end time:inXSDDateTime ?dte .
        """.stripMargin

    override protected val whereClause: String = "?t time:inXSDDateTime ?d ."
}
