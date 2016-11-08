package services.discovery.components.visualizer

class GoogleMapsVisualizer extends SimpleVisualizer {

    override protected val prefixes: String =
        """
          |PREFIX s: <http://schema.org/>""".stripMargin

    override protected val whereClause: String =
        """
          |?something s:geo ?g .
          |?g a s:GeoCoordinates ;
          |    s:latitude ?lat ;
          |    s:longitude ?lng .""".stripMargin
}