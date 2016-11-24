package services.discovery.components.transformer

class Schema_GeoCoordinates2Geo_SpatialThing extends SparqlUpdateTransformer {

    protected override val prefixes =
        """
          |PREFIX geo: <http://www.w3.org/2003/01/geo/wgs84_pos#>
          |PREFIX s: <http://schema.org/>
        """.stripMargin
    protected override val deleteClause =
        """
          |?s s:geo ?geo .
          |
          |?geo s:longitude ?long ;
          |    s:latitude ?lat .
        """.stripMargin
    protected override val insertClause =
        """
          |?s geo:location ?geo .
          |
          |?geo a geo:SpatialThing ;
          |    geo:long ?long ;
          |    geo:lat ?lat .
        """.stripMargin
    protected override val whereClause =
        """
          |?s s:geo ?geo .
          |
          |?geo s:longitude ?long ;
          |    s:latitude ?lat .
          |""".stripMargin
}
