package services.discovery.components.visualizer

class TemporalEntityTimeIntervalVisualizer extends SimpleVisualizer {

    override protected val prefixes: String =
        """
          |  [] time:hasBeginning ?beginning ;
          |    time:hasEnd ?end .
          |
          |  ?beginning time:inXSDDateTime ?dtb .
          |
          |  ?end time:inXSDDateTime ?dte .
        """.stripMargin

    override protected val whereClause: String = "?t time:inXSDDateTime ?d ."
}
