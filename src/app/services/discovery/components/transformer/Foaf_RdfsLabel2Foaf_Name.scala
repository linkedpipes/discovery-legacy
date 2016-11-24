package services.discovery.components.transformer

class Foaf_RdfsLabel2Foaf_Name extends SparqlUpdateTransformer {

    protected override val whereClause =
        """
          |VALUES ?type { foaf:Agent foaf:Group foaf:Person foaf:Organization }
          |  ?agent a ?type ;
          |    rdfs:label ?label .""".stripMargin
    protected override val deleteClause = "?agent rdfs:label ?label ."
    protected override val insertClause =
        """
          |?agent foaf:name ?label .
        """.stripMargin
    protected override val prefixes =
        """
          |PREFIX foaf: <http://xmlns.com/foaf/0.1/>
          |PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
        """.stripMargin
}
