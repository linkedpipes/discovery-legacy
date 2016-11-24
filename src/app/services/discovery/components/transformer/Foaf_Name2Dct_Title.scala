package services.discovery.components.transformer

class Foaf_Name2Dct_Title extends SparqlUpdateTransformer {

    protected override val whereClause =
        """
          |?agent foaf:name ?name .""".stripMargin
    protected override val deleteClause = "?agent foaf:name ?name ."
    protected override val insertClause =
        """
          | ?agent dct:title ?name .
        """.stripMargin
    protected override val prefixes =
        """
          |PREFIX foaf: <http://xmlns.com/foaf/0.1/>
        """.stripMargin
}
