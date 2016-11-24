package services.discovery.components.extractor

class AresExtractor extends SimpleExtractor {

    override protected val prefixes: String =
        """
          |PREFIX gr: <http://purl.org/goodrelations/v1#>
          |PREFIX s: <http://schema.org/>
        """.stripMargin

    override protected val constructClause: String =
        """
          |  ?be a gr:BusinessEntity ;
          |    gr:legalName ?legalName ;
          |    s:address ?address .
        """.stripMargin

    override protected val whereClause: String =
        """
          |  ?be a gr:BusinessEntity ;
          |    gr:legalName ?legalName ;
          |    s:address ?address .
        """.stripMargin
}
