package services.discovery.components.extractor

class DcatDatasetExtractor extends SimpleExtractor {

    override protected val prefixes: String = """PREFIX dcat: <http://www.w3.org/ns/dcat#>""".stripMargin

    override protected val constructClause: String =
        """
          |?s a dcat:Dataset ;
          |    ?p ?o ;
          |    dcat:theme ?theme. """.stripMargin

    override protected val whereClause: String =
        """
          |?s a dcat:Dataset ;
          |    ?p ?o ;
          |    dcat:theme ?theme .
          |
          |FILTER(?theme = <http://publications.europa.eu/resource/authority/data-theme/INTR>) """.stripMargin
}