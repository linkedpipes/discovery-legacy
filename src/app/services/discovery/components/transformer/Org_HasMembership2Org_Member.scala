package services.discovery.components.transformer

class Org_HasMembership2Org_Member extends SparqlUpdateTransformer {

    protected override val prefixes =
        """
          |PREFIX org: <http://www.w3.org/ns/org#>
        """.stripMargin
    protected override val deleteClause =
        """
          |?s org:hasMembership ?o .
        """.stripMargin
    protected override val insertClause =
        """
          |?o org:member ?s .
        """.stripMargin
    protected override val whereClause =
        """
          |?s org:hasMembership ?o .
          |""".stripMargin
}
