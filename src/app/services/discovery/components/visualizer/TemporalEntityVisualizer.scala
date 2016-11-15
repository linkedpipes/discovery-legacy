package services.discovery.components.visualizer

class TemporalEntityVisualizer extends SimpleVisualizer {

    override protected val prefixes: String = "PREFIX time: <http://www.w3.org/2006/time#>"

    override protected val whereClause: String = "?t time:inXSDDateTime ?d ."
}
