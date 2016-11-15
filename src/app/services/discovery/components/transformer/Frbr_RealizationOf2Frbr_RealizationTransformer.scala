package services.discovery.components.transformer

class Frbr_RealizationOf2Frbr_RealizationTransformer extends SparqlUpdateTransformer {

    protected override val whereClause = "?expression frbr:realizationOf ?work ."
    protected override val deleteClause = "?expression frbr:realizationOf ?work ."
    protected override val insertClause =
        """
          |?work frbr:realization ?expression .
        """.stripMargin
    protected override val prefixes =
        """
          |PREFIX frbr: <http://purl.org/vocab/frbr/core#>
          |PREFIX dct: <http://purl.org/dc/terms/>
        """.stripMargin
}
