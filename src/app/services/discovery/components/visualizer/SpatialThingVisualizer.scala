package services.discovery.components.visualizer

class SpatialThingVisualizer extends SimpleVisualizer {

    override protected val prefixes: String =
        """
          |PREFIX geo: <http://www.w3.org/2003/01/geo/wgs84_pos#>
        """.stripMargin

    override protected val whereClause: String =
        """
          |[] a geo:SpatialThing ;
          |    geo:long ?long ;
          |    geo:lat ?lat .""".stripMargin
}