package services.discovery.components.extractor

class NomismaOrgPersonsExtractor extends SimpleExtractor {

    override protected val prefixes: String =
        """
          |PREFIX rdf:    <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
          |PREFIX nm:    <http://nomisma.org/id/>
          |PREFIX nmo:    <http://nomisma.org/ontology#>
          |PREFIX skos:    <http://www.w3.org/2004/02/skos/core#>
          |PREFIX dcterms:    <http://purl.org/dc/terms/>
          |PREFIX foaf: <http://xmlns.com/foaf/0.1/>
          |PREFIX org: <http://www.w3.org/ns/org#>
          |PREFIX geo: <http://www.w3.org/2003/01/geo/wgs84_pos#>
        """.stripMargin

    override protected val constructClause: String =
        """
          |?person ?personProp ?personObj ;
          |        org:hasMembership ?membership .
          |
          |    ?membership org:role ?role .
          |
          |    ?type nmo:hasAuthority ?person ;
          |        nmo:hasDenomination ?denomination ;
          |        nmo:hasStartDate ?startDate ;
          |        nmo:hasEndDate ?endDate ;
          |        nmo:hasManufacture ?manufacture ;
          |        nmo:hasMaterial ?material ;
          |        nmo:hasMint ?mint ;
          |        nmo:hasObverse ?obverse ;
          |        nmo:hasReverse ?reverse ;
          |        nmo:hasRegion ?region ;
          |        nmo:representsObjectType ?objectType ;
          |        skos:prefLabel ?prefLabel ;
          |        skos:definition ?definition .
          |
          |    ?denomination skos:prefLabel ?denominationLabel ;
          |        skos:definition ?denominationDefinition .
          |
          |    ?manufacture skos:prefLabel ?manufactureLabel ;
          |        skos:definition ?manufactureDefinition .
          |
          |    ?material skos:prefLabel ?materialLabel ;
          |        skos:definition ?materialDefinition .
          |
          |    ?mint skos:prefLabel ?mintLabel ;
          |        skos:definition ?mintDefinition ;
          |        dcterms:isPartOf ?mintPartOf ;
          |        skos:broader ?mintBroader ;
          |        geo:location ?mintLocation .
          |
          |    ?mintLocation dcterms:isPartOf ?mintLocationPartOf ;
          |        geo:lat ?mintLocationLat ;
          |        geo:long ?mintLocationLong .
          |
          |    ?obverse dcterms:description ?obverseDescription .
          |
          |    ?reverse dcterms:description ?reverseDescription .
          |
          |    ?region  skos:prefLabel ?regionLabel ;
          |        skos:definition ?regionDefinition ;
          |        dcterms:isPartOf ?regionPartOf ;
          |        skos:broader ?regionBroader .
          |
          |    ?objectType skos:prefLabel ?objectTypeLabel .
        """.stripMargin

    override protected val whereClause: String =
        """
          |    ?person ?personProp ?personObj ;
          |        org:hasMembership ?membership .
          |
          |    ?membership org:role ?role .
          |
          |    ?type nmo:hasAuthority ?person ;
          |        nmo:hasDenomination ?denomination ;
          |        nmo:hasStartDate ?startDate ;
          |        nmo:hasEndDate ?endDate ;
          |        nmo:hasManufacture ?manufacture ;
          |        nmo:hasMaterial ?material ;
          |        nmo:hasMint ?mint ;
          |        nmo:hasObverse ?obverse ;
          |        nmo:hasReverse ?reverse ;
          |        nmo:hasRegion ?region ;
          |        nmo:representsObjectType ?objectType ;
          |        skos:prefLabel ?prefLabel ;
          |        skos:definition ?definition .
          |
          |    ?denomination skos:prefLabel ?denominationLabel ;
          |        skos:definition ?denominationDefinition .
          |
          |    ?manufacture skos:prefLabel ?manufactureLabel ;
          |        skos:definition ?manufactureDefinition .
          |
          |    ?material skos:prefLabel ?materialLabel ;
          |        skos:definition ?materialDefinition .
          |
          |    ?mint skos:prefLabel ?mintLabel ;
          |        skos:definition ?mintDefinition ;
          |        dcterms:isPartOf ?mintPartOf ;
          |        skos:broader ?mintBroader ;
          |        geo:location ?mintLocation .
          |
          |    ?mintLocation dcterms:isPartOf ?mintLocationPartOf ;
          |        geo:lat ?mintLocationLat ;
          |        geo:long ?mintLocationLong .
          |
          |    ?obverse dcterms:description ?obverseDescription .
          |
          |    ?reverse dcterms:description ?reverseDescription .
          |
          |    ?region  skos:prefLabel ?regionLabel ;
          |        skos:definition ?regionDefinition ;
          |        dcterms:isPartOf ?regionPartOf ;
          |        skos:broader ?regionBroader .
          |
          |    ?objectType skos:prefLabel ?objectTypeLabel .
        """.stripMargin
}
