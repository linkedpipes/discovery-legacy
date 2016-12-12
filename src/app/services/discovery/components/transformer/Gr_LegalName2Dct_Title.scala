package services.discovery.components.transformer

class Gr_LegalName2Dct_Title extends SparqlUpdateTransformer {

    protected override val prefixes =
        """
          |PREFIX gr: <http://purl.org/goodrelations/v1#>
          |PREFIX dct: <http://purl.org/dc/terms/>
        """.stripMargin

    protected override val deleteClause = "?be gr:legalName ?name ."

    protected override val insertClause =
        """
          |?be dct:title ?name .
        """.stripMargin

    protected override val whereClause =
        """
          |  ?be gr:legalName ?name .
          |  FILTER NOT EXISTS {
          |    ?be dct:title ?name .
          |  }
        """.stripMargin
}
