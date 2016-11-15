package services.discovery.components.extractor

class LegislationCzActsExtractor extends SimpleExtractor {

    override protected val prefixes: String =
        """
          |PREFIX lex: <http://purl.org/lex#>
          |PREFIX dct: <http://purl.org/dc/terms/>
        """.stripMargin

    override protected val constructClause: String =
        """
          | ?work a lex:Act ;
          |   dct:title ?title ;
          |   dct:issued ?date .""".stripMargin

    override protected val whereClause: String =
        """
          | ?work a lex:Act ;
          |   dct:title ?title ;
          |   dct:issued ?date .""".stripMargin
}