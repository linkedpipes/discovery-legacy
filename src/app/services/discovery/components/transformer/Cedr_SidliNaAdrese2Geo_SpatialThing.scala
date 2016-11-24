package services.discovery.components.transformer

class Cedr_SidliNaAdrese2Geo_SpatialThing extends SparqlUpdateTransformer {

    protected override val whereClause =
        """
          | ?s cedr:sidliNaAdrese ?misto ;
          |    dct:title ?title .
          |
          |  BIND(CONCAT("Registered address of  ", STR(?title)) AS ?abstractionLabel)
          |
          |  ?misto geo:location ?geo .
          |
          |  ?geo a geo:SpatialThing ;
          |    geo:long ?long ;
          |    geo:lat ?lat .
        """.stripMargin
    protected override val deleteClause = """?s cedr:sidliNaAdrese ?misto ."""
    protected override val insertClause =
        """
          |?s lpviz:hasAbstraction ?misto .
          |
          |?misto rdfs:label ?abstractionLabel .
          |
        """.stripMargin
    protected override val prefixes =
        """
          | PREFIX geo: <http://www.w3.org/2003/01/geo/wgs84_pos#>
          | PREFIX dct: <http://purl.org/dc/terms/>
          | PREFIX cedr: <http://cedropendata.mfcr.cz/c3lod/cedr/vocabCEDR#>""".stripMargin
}
