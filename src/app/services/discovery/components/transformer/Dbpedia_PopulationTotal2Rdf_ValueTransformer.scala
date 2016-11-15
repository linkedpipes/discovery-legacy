package services.discovery.components.transformer

class Dbpedia_PopulationTotal2Rdf_ValueTransformer extends SparqlUpdateTransformer {

    protected override val whereClause = "?place dbo:populationTotal ?pop ."
    protected override val deleteClause = whereClause
    protected override val insertClause = "?place rdf:value ?pop ."
    protected override val prefixes =
        """
          | PREFIX owl: <http://www.w3.org/2002/07/owl#>
          | PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
          | PREFIX dbo: <http://dbpedia.org/ontology/>""".stripMargin
}
