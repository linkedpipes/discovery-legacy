package services.discovery.components.extractor

class LegislationCzActsVersionsExtractor extends SimpleExtractor {

    override protected val prefixes: String =
        """
          |PREFIX lex: <http://purl.org/lex#>
          |PREFIX frbr: <http://purl.org/vocab/frbr/core#>
          |PREFIX dct: <http://purl.org/dc/terms/>
        """.stripMargin

    override protected val constructClause: String =
        """
          | ?work a lex:Act ;
          |   dct:title ?title ;
          |   dct:issued ?date .
          |
          | ?expression a frbr:Expression ;
          |   frbr:realizationOf ?work ;
          |   dct:valid ?valid .
        """.stripMargin

    override protected val whereClause: String =
        """
          | ?work a lex:Act ;
          |   dct:title ?title ;
          |   dct:issued ?date .
          |
          | ?expression frbr:realizationOf ?work ;
          |   dct:valid ?valid .""".stripMargin
}