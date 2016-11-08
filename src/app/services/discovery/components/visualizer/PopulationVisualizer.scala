package services.discovery.components.visualizer

class PopulationVisualizer extends SimpleVisualizer {

    override protected val prefixes: String =
        """
          |PREFIX s: <http://schema.org/>
          |PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>""".stripMargin

    override protected val whereClause: String =
        """
          |?p rdf:value ?populationCount ;
          |    s:name ?placeName ;
          |    s:geo [
          |        s:latitude ?lat ;
          |        s:longitude ?lng
          |] .""".stripMargin
}