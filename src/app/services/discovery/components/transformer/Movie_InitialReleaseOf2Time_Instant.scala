package services.discovery.components.transformer

class Movie_InitialReleaseOf2Time_Instant extends SparqlUpdateTransformer {

    protected override val prefixes =
        """
          |PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
          |PREFIX movie: <http://data.linkedmdb.org/resource/movie/>
          |PREFIX dct: <http://purl.org/dc/terms/>
          |PREFIX time: <http://www.w3.org/2006/time#>
          |PREFIX lpviz: <http://visualization.linkedpipes.com/ontology/>
        """.stripMargin

    protected override val deleteClause =
        """
          |?m movie:initial_release_date ?date ;
          |    dct:date ?date .
        """.stripMargin

    protected override val insertClause =
        """
          |?m lpviz:hasAbstraction ?abstraction .
          |
          |  ?abstraction a time:Instant ;
          |    time:inXSDDateTime ?date ;
          |	rdfs:label ?abstractionLabel .
        """.stripMargin

    protected override val whereClause =
        """
          |?m movie:initial_release_date ?date .
          |
          |  OPTIONAL {
          |	?m dct:title ?title .
          |	 BIND(CONCAT("Initial release date of ", STR(?title)) AS ?abstractionLabel)
          |  }
          |
          |  OPTIONAL {
          |    ?m dct:date ?date .
          |  }
          |
          |  BIND(IRI(CONCAT(STR(?m), "/abstraction/linkedmdb-initial-release-of2time-Instant")) AS ?abstraction)
        """.stripMargin
}
