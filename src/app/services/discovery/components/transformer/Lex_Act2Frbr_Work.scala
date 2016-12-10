package services.discovery.components.transformer

class Lex_Act2Frbr_Work extends SparqlUpdateTransformer {

    protected override val prefixes =
        """
          |PREFIX lex: <http://purl.org/lex#>
          |PREFIX frbr: <http://purl.org/vocab/frbr/core#>
        """.stripMargin

    protected override val deleteClause =
        """
          |?s a lex:Act .
        """.stripMargin

    protected override val insertClause =
        """
          | ?s a frbr:Work .
        """.stripMargin

    protected override val whereClause =
        """
          |?s a lex:Act .
        """.stripMargin
}
