package services.discovery.components.transformer

class Movie_ProducerName2Foaf_Name extends SparqlUpdateTransformer {

    protected override val prefixes =
        """
          |PREFIX foaf: <http://xmlns.com/foaf/0.1/>
          |PREFIX movie: <http://data.linkedmdb.org/resource/movie/>
        """.stripMargin
    protected override val deleteClause =
        """
          |?person movie:producer_name ?name .
        """.stripMargin
    protected override val insertClause =
        """
          |?person a foaf:Person ;
          |    foaf:name ?name .
        """.stripMargin
    protected override val whereClause =
        """
          |?person movie:producer_name ?name .""".stripMargin
}
