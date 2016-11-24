package services.discovery.components.transformer

class Foaf_Maker2Foaf_Made extends SparqlUpdateTransformer {

    protected override val whereClause =
        """
          |?thing foaf:maker ?maker .""".stripMargin
    protected override val deleteClause = "?thing foaf:maker ?maker ."
    protected override val insertClause =
        """
          | ?maker foaf:made ?thing .
        """.stripMargin
    protected override val prefixes =
        """
          |PREFIX foaf: <http://xmlns.com/foaf/0.1/>
        """.stripMargin
}
