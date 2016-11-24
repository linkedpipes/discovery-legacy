package services.discovery.components.extractor

class SubsidiesCzCedrExtractor extends SimpleExtractor {

    override protected val prefixes: String =
        """
          |PREFIX cedr: <http://cedropendata.mfcr.cz/c3lod/cedr/vocabCEDR#>
          |PREFIX spa: <http://cedropendata.mfcr.cz/c3lod/isdp/vocabIsdp/space/v1#>
          |PREFIX gr: <http://purl.org/goodrelations/v1#>
        """.stripMargin

    override protected val constructClause: String =
        """
          |  ?dotace a cedr:Dotace ;
          |    cedr:byloRozhodnuto ?rozhodnuti ;
          |    ?dotacep ?dotaceo .
          |
          |  ?rozhodnuti
          |    cedr:castkaRozhodnuta ?castka ;
          |    cedr:rokRozhodnuti ?rokRozhodnuti .
          |
          |  ?prijemce
          |    cedr:obdrzelDotaci ?dotace ;
          |    cedr:sidliNaAdrese ?adresa ;
          |    gr:legalName ?nazevPrijemce .
          |
          |  ?adresa
          |    spa:adresniMistoKod ?adresniMistoKod .
        """.stripMargin

    override protected val whereClause: String =
        """
          |?dotace a cedr:Dotace ;
          |    cedr:byloRozhodnuto ?rozhodnuti ;
          |    ?dotacep ?dotaceo .
          |    
          |  ?rozhodnuti
          |    cedr:castkaRozhodnuta ?castka ;
          |    cedr:rokRozhodnuti ?rokRozhodnuti .
          |    
          |  ?prijemce
          |    cedr:obdrzelDotaci ?dotace ;
          |    cedr:sidliNaAdrese ?adresa ;
          |    gr:legalName ?nazevPrijemce .
          |    
          |  ?adresa
          |    spa:adresniMistoKod ?adresniMistoKod .
        """.stripMargin
}
