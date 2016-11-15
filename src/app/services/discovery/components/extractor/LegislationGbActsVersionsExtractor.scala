package services.discovery.components.extractor

class LegislationGbActsVersionsExtractor extends SimpleExtractor {

    override protected val prefixes: String =
        """
          |PREFIX frbr: <http://purl.org/vocab/frbr/core#>
          |PREFIX dct: <http://purl.org/dc/terms/>
        """.stripMargin

    override protected val constructClause: String =
        """
          | ?work a frbr:Work ;
          |   dct:title ?title ;
          |   dct:created ?date ;
          |   frbr:realization ?expression .
          |
          | ?expression a frbr:Expression ;
          |   dct:hasVersion ?version .
          |
          | ?version a frbr:Expression ;
          |	dct:valid ?valid .""".stripMargin

    override protected val whereClause: String =
        """
          | ?work a frbr:Work ;
          |   dct:title ?title ;
          |   dct:created ?date ;
          |   frbr:realization ?expression .
          |
          | ?expression a frbr:Expression ;
          |   dct:hasVersion ?version .
          |
          | ?version a frbr:Expression ;
          |   dct:valid ?valid .""".stripMargin
}