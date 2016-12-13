package services.discovery.components.transformer

class Bibtex_Date2Dct_Issued extends SparqlUpdateTransformer {

    protected override val prefixes =
        """
          | PREFIX bibtex: <http://zeitkunst.org/bibtex/0.1/bibtex.owl#>
          |PREFIX dct: <http://purl.org/dc/terms/>
        """.stripMargin
    
    protected override val deleteClause =
        """
          |   ?s bibtex:hasYear ?year .
        """.stripMargin
    
    protected override val insertClause =
        """
          |   ?s dct:issued ?year .
        """.stripMargin
    
    protected override val whereClause =
        """
          |  ?s bibtex:hasYear ?year .
        """.stripMargin
}
