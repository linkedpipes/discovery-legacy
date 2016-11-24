package services.discovery.components.transformer

class Nomisma_StartDateEndDate2Time_Interval extends SparqlUpdateTransformer {

    protected override val prefixes =
        """
          |PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
          |PREFIX time: <http://www.w3.org/2006/time#>
          |PREFIX skos: <http://www.w3.org/2004/02/skos/core#>
          |PREFIX nmo: <http://nomisma.org/ontology#>
          |PREFIX lpviz: <http://visualization.linkedpipes.com/ontology/>
        """.stripMargin
    protected override val deleteClause =
        """
          |?s nmo:hasStartDate ?sd ;
          |    nmo:hasEndDate ?ed .
        """.stripMargin
    protected override val insertClause =
        """
          |?s lpviz:hasAbstraction [
          |    a time:Interval ;
          |    time:hasBeginning [
          |        a time:Instant ;
          |        time:inXSDDateTime ?start
          |    ];
          |    time:hasEnd [
          |        a time:Instant ;
          |        time:inXSDDateTime ?end
          |    ];
          |    rdfs:label ?abstractionLabel
          |] .
        """.stripMargin
    protected override val whereClause =
        """
          |?s nmo:hasStartDate ?start ;
          |    nmo:hasEndDate ?end ;
          |    skos:prefLabel ?label .
          |
          |BIND(CONCAT("Existence of ", STR(?label)) AS ?abstractionLabel)
          |""".stripMargin
}
