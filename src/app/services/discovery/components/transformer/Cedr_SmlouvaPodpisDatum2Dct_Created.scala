package services.discovery.components.transformer

class Cedr_SmlouvaPodpisDatum2Dct_Created extends SparqlUpdateTransformer {

    protected override val whereClause = """?s cedr:smlouvaPodpisDatum ?dateTime .""".stripMargin
    protected override val deleteClause = """?s cedr:smlouvaPodpisDatum ?dateTime ."""
    protected override val insertClause = """?s dct:created ?dateTime ."""
    protected override val prefixes =
        """
          |PREFIX cedr: <http://cedropendata.mfcr.cz/c3lod/cedr/vocabCEDR#>
          |PREFIX dct: <http://purl.org/dc/terms/>""".stripMargin
}
