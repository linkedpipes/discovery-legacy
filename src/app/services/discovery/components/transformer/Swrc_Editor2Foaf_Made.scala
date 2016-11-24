package services.discovery.components.transformer

class Swrc_Editor2Foaf_Made extends SparqlUpdateTransformer {

    protected override val prefixes =
        """
          |PREFIX foaf: <http://xmlns.com/foaf/0.1/>
          |PREFIX swrc: <http://swrc.ontoware.org/ontology#>
        """.stripMargin
    protected override val deleteClause =
        """
          |?thing swrc:editor ?editor .
        """.stripMargin
    protected override val insertClause =
        """
          |?editor foaf:made ?thing .
        """.stripMargin
    protected override val whereClause =
        """
          |?thing swrc:editor ?editor .
          |""".stripMargin
}
