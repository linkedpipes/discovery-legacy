package services.discovery.components.transformer

class LinkedMdb_InitialReleaseOf2Time_Instant extends SparqlUpdateTransformer {

    protected override val prefixes =
        """
          |PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
          |PREFIX movie: <http://data.linkedmdb.org/resource/movie/>
          |PREFIX time: <http://www.w3.org/2006/time#>
          |PREFIX dct: <http://purl.org/dc/terms/>
          |PREFIX lpviz: <http://visualization.linkedpipes.com/ontology/>
        """.stripMargin
    protected override val deleteClause =
        """
          |?m movie:initial_release_date ?dateTime .
        """.stripMargin
    protected override val insertClause =
        """
          |?m lpviz:hasAbstraction [
          |    a time:Instant ;
          |    time:inXSDDateTime ?dateTime ;
          |    rdfs:label ?abstractionLabel
          |] .
        """.stripMargin
    protected override val whereClause =
        """
          |?m movie:initial_release_date ?date .
          |
          |OPTIONAL {
          |    ?s dct:title ?title .
          |    BIND(CONCAT("Initial release date of ", STR(?title)) AS ?abstractionLabel)
          |}""".stripMargin
}