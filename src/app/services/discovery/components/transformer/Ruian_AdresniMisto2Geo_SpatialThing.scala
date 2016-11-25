package services.discovery.components.transformer

class Ruian_AdresniMisto2Geo_SpatialThing extends SparqlUpdateTransformer {

    protected override val prefixes =
        """
          |PREFIX geo: <http://www.w3.org/2003/01/geo/wgs84_pos#>
          |PREFIX ruian: <http://ruian.linked.opendata.cz/ontology/>
          |PREFIX s: <http://schema.org/>
        """.stripMargin
    protected override val deleteClause =
        """
          |?misto ruian:adresniBod ?bod .
          |
          |?bod s:geo ?geo .
          |
          |?geo s:longitude ?long ;
          |    s:latitude ?lat .
        """.stripMargin
    protected override val insertClause =
        """
          |?misto geo:location ?geo .
          |
          |?geo a geo:SpatialThing ;
          |    geo:long ?long ;
          |    geo:lat ?lat .
        """.stripMargin
    protected override val whereClause =
        """
          |?misto ruian:adresniBod ?bod .
          |
          |?bod s:geo ?geo .
          |
          |?geo s:longitude ?long ;
          |    s:latitude ?lat .
          |""".stripMargin
}
