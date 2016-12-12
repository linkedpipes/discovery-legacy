package services.discovery.components.transformer

class Foaf_SkosPrefLabel2Foaf_Name extends SparqlUpdateTransformer {

    protected override val prefixes =
        """
          |PREFIX foaf: <http://xmlns.com/foaf/0.1/>
          |PREFIX skos: <http://www.w3.org/2004/02/skos/core>
        """.stripMargin

    protected override val deleteClause =
        """
          |?agent skos:prefLabel ?prefLabel .
        """.stripMargin

    protected override val insertClause =
        """
          |?agent foaf:name ?prefLabel .
        """.stripMargin

    protected override val whereClause =
        """
          |VALUES ?type { foaf:Agent foaf:Group foaf:Person foaf:Organization }
          |  ?agent a ?type ;
          |    skos:prefLabel ?prefLabel .
          |  FILTER NOT EXISTS {
          |    ?agent foaf:name ?label .
          |  }
        """.stripMargin
}
