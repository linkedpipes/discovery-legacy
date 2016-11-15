package services.discovery.components.transformer

class Ruian_DefinicniBod2Schema_PlaceTransformer extends SparqlUpdateTransformer {

    protected override val deleteClause =
        """
          |?p ruian:definicniBod ?definicniBod .
          |
          |?definicniBod rdf:type ogcgml:MultiPoint ;
          |    ogcgml:pointMember ?pointMember .
          |
          |?pointMember rdf:type ogcgml:Point ;
          |    s:geo ?geo .
          |?geo rdf:type s:GeoCoordinates ;
          |    s:longitude ?lng ;
          |    s:latitude ?lat .""".stripMargin
    protected override val insertClause =
        """
          |?p s:geo [
          |    rdf:type s:GeoCoordinates ;
          |    s:longitude ?lng ;
          |    s:latitude  ?lat
          |] .""".stripMargin
    protected override val whereClause =
        """
          |?p a dbo:PopulatedPlace ;
          |    ruian:definicniBod ?definicniBod .
          |
          |?definicniBod rdf:type ogcgml:MultiPoint ;
          |    ogcgml:pointMember ?pointMember .
          |
          |?pointMember rdf:type ogcgml:Point ;
          |    s:geo ?geo .
          |?geo rdf:type s:GeoCoordinates ;
          |    s:longitude ?lng ;
          |    s:latitude ?lat .""".stripMargin
    protected override val prefixes =
        """
          |PREFIX s: <http://schema.org/>
          |PREFIX dbo: <http://dbpedia.org/ontology/>
          |PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
          |PREFIX ruian: <http://ruian.linked.opendata.cz/ontology/>
          |PREFIX ogcgml:  <http://www.opengis.net/ont/gml#>""".stripMargin
}
