import scala.io.Source

object AppGen {
    def main(args: Array[String]) = {

        val appId = args(0)
        val appTitle = args(1)
        val appDescription = args(2)
        val queryFilePath = args(3)
        val query = Source.fromFile(queryFilePath).mkString

        val template =
            s"""
               | @prefix dcterms: <http://purl.org/dc/terms/> .
               | @prefix ldvm:       <http://linked.opendata.cz/ontology/ldvm/> .
               | @prefix application:  <http://linked.opendata.cz/ldcp/resource/ldvm/application/$appId/> .
               | @prefix configuration-vocabulary:  <http://linked.opendata.cz/vocabulary/ldvm/application/$appId/configuration/> .
               | @prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .
               | @prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .
               |
               | application:template a ldvm:ApplicationTemplate ;
               |     dcterms:title "$appTitle"@en;
               |     dcterms:description "$appDescription"@en;
               |     ldvm:componentConfigurationTemplate application:defaultConfiguration ;
               |     ldvm:inputTemplate application:input ;
               |     ldvm:feature application:defaultFeature .
               |
               | configuration-vocabulary:Configuration a rdfs:Class ;
               |     rdfs:label "Class of configurations of $appTitle"@en;
               |     rdfs:subClassOf ldvm:ComponentConfiguration .
               |
               | application:defaultConfiguration a configuration-vocabulary:Configuration ;
               |     dcterms:title "Default configuration for $appTitle"@en ;
               |     ldvm:configurationQuery \"\"\"
               |         PREFIX dcterms: <http://purl.org/dc/terms/>
               |         PREFIX application:  <http://linked.opendata.cz/ldcp/resource/ldvm/application/$appId/>
               |         PREFIX configuration-vocabulary: <http://linked.opendata.cz/vocabulary/ldvm/application/$appId/configuration/>
               |
               |         CONSTRUCT {
               |             ?config a configuration-vocabulary:Configuration ;
               |                 dcterms:title ?title ;
               |         } WHERE {
               |             ?config a configuration-vocabulary:Configuration .
               |             OPTIONAL { ?config dcterms:title ?title . }
               |         }
               |     \"\"\" .
               |
               | application:input a ldvm:InputDataPortTemplate ;
               |     dcterms:title "Input of $appTitle" .
               |
               | application:defaultFeature a ldvm:MandatoryFeature ;
               |     dcterms:title "The default feature of $appTitle" ;
               |     ldvm:descriptor application:defaultDescriptor .
               |
               | application:defaultDescriptor a ldvm:Descriptor ;
               |     dcterms:title "Checks if default feature of $appTitle can be applied." ;
               |     ldvm:query \"\"\"\n$query\n\"\"\" ;
               |     ldvm:appliesTo application:input .
               """.stripMargin

            println(template)
    }
}