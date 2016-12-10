package services.discovery.components.application

class PersonalProfilesApplication extends SimpleApplication {

    override protected val prefixes: String =
        """
          |PREFIX geo: <http://www.w3.org/2003/01/geo/wgs84_pos#>
          |PREFIX foaf: <http://xmlns.com/foaf/0.1/>
          |PREFIX time: <http://www.w3.org/2006/time#>
          |PREFIX lpviz: <http://visualization.linkedpipes.com/ontology/>
          |PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
        """.stripMargin

    override protected val whereClause: String =
        """
          |?agent foaf:name ?agentName ;
          |    foaf:made ?thing .
          |
          |  {
          |    ?thing lpviz:hasAbstraction ?timeAbstraction .
          |
          |    ?timeAbstraction time:inXSDDateTime ?dt .
          |
          |    OPTIONAL {
          |      ?timeAbstraction rdfs:label ?abstractionLabel .
          |    }
          |  } UNION {
          |    ?thing lpviz:hasAbstraction ?timeAbstraction .
          |
          |    ?timeAbstraction time:hasBeginning ?beginning ;
          |        time:hasEnd ?end .
          |
          |    OPTIONAL {
          |      ?timeAbstraction rdfs:label ?abstractionLabel .
          |    }
          |
          |    ?beginning time:inXSDDateTime ?dtb .
          |
          |    ?end time:inXSDDateTime ?dte .
          |  } UNION {
          |    ?thing lpviz:hasAbstraction ?place .
          |
          |    ?place geo:location ?geo .
          |
          |    OPTIONAL {
          |      ?place rdfs:label ?abstractionLabel .
          |    }
          |
          |    ?geo a geo:SpatialThing ;
          |      geo:long ?long ;
          |      geo:lat ?lat .
          |  }""".stripMargin
}