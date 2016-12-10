package services.discovery.components.application

class PlacesOnMapApplication extends SimpleApplication {

    override protected val prefixes: String =
        """
          |PREFIX geo: <http://www.w3.org/2003/01/geo/wgs84_pos#>
        """.stripMargin

    override protected val whereClause: String =
        """
          |  [] geo:long ?long ;
          |    geo:lat ?lat .
        """.stripMargin
}