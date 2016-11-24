package services.discovery.components.transformer

class Nomisma_HasAuthority2Foaf_Made extends SparqlUpdateTransformer {

    protected override val prefixes =
        """
          |PREFIX foaf: <http://xmlns.com/foaf/0.1/>
          |PREFIX nmo: <http://nomisma.org/ontology#>
        """.stripMargin
    protected override val deleteClause =
        """
          |?type nmo:hasAuthority ?authority .
        """.stripMargin
    protected override val insertClause =
        """
          |?authority foaf:made ?type .
        """.stripMargin
    protected override val whereClause =
        """
          |?type nmo:hasAuthority ?authority .""".stripMargin
}
