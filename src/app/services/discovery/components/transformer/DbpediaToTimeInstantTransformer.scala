package services.discovery.components.transformer

class DbpediaToTimeInstantTransformer extends SparqlUpdateTransformer {

    protected override val whereClause = "?t dbo:date ?d ."
    protected override val deleteClause = whereClause
    protected override val insertClause = "?t time:inXSDDateTime ?d ."
    protected override val prefixes =
        """
          |PREFIX dbo: <http://dbpedia.org/ontology/>
          |PREFIX time: <http://www.w3.org/2006/time#>""".stripMargin
}
