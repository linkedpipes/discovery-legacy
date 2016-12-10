package services.discovery.components.application

class TimeIntervalsApplication extends SimpleApplication {

    override protected val prefixes: String =
        """
          |PREFIX time: <http://www.w3.org/2006/time#>
        """.stripMargin

    override protected val whereClause: String =
        """
          |  ?i time:hasBeginning ?beginning ;
          |    time:hasEnd ?end .
          |
          |  ?beginning time:inXSDDateTime ?dtb .
          |
          |  ?end time:inXSDDateTime ?dte .
        """.stripMargin
}
