package services.discovery.components.transformer

class Frbr_Realization2Dct_HasVersionTransformer extends SparqlUpdateTransformer {

    protected override val whereClause = "?work frbr:realization ?expression ."
    protected override val deleteClause = "?work frbr:realization ?expression ."
    protected override val insertClause =
        """
          |?work dct:hasVersion ?expression .
        """.stripMargin
    protected override val prefixes =
        """
          |PREFIX frbr: <http://purl.org/vocab/frbr/core#>
          |PREFIX dct: <http://purl.org/dc/terms/>
        """.stripMargin
}
