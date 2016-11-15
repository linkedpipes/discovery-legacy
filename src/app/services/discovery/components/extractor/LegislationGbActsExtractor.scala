package services.discovery.components.extractor

class LegislationGbActsExtractor extends SimpleExtractor {

    override protected val prefixes: String =
        """
          |PREFIX frbr: <http://purl.org/vocab/frbr/core#>
          |PREFIX dct: <http://purl.org/dc/terms/>
        """.stripMargin

    override protected val constructClause: String =
        """
          | ?work a frbr:Work ;
          |   dct:title ?title ;
          |   dct:created ?date .""".stripMargin

    override protected val whereClause: String =
        """
          | ?work a frbr:Work ;
          |   dct:title ?title ;
          |   dct:created ?date .""".stripMargin
}