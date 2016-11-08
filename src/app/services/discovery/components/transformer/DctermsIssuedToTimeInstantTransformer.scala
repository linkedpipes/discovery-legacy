package services.discovery.components.transformer

class DctermsIssuedToTimeInstantTransformer extends SparqlUpdateTransformer {

    protected override val whereClause = "?t dcterms:issued ?d ."
    protected override val deleteClause = whereClause
    protected override val insertClause = "?t time:inXSDDateTime ?d ."
    protected override val prefixes =
        """
          |PREFIX dbo: <http://dbpedia.org/ontology/>
          |PREFIX dcterms: <http://purl.org/dc/terms/>
          |PREFIX time: <http://www.w3.org/2006/time#>""".stripMargin
}
