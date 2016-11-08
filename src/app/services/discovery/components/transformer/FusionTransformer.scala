package services.discovery.components.transformer

class FusionTransformer extends SparqlUpdateTransformer {

    protected override val deleteClause = "?entity1 owl:sameAs ?entity2 ."
    protected override val insertClause =
        """
          |?entity2 ?prop1 ?subj1 .
          |?entity2 ?prop2 ?subj2.""".stripMargin
    protected override val whereClause =
        """
          |?entity1 owl:sameAs ?entity2 ;
          |    ?prop1 ?subj1 .
          |?entity2 ?prop2 ?subj2 .
          |FILTER (?prop1 != owl:sameAs)""".stripMargin
    protected override val prefixes =
        """
          |PREFIX owl: <http://www.w3.org/2002/07/owl#>
          |PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
          |PREFIX dbo: <http://dbpedia.org/ontology/>""".stripMargin
}
