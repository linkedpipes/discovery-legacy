import play.api.libs.json.Json

import scala.io.Source
import better.files._
import java.io.{File => JFile}

val lov = Seq(
    "dcterms",
    "dce",
    "foaf",
    "vann",
    "skos",
    "cc",
    "vs",
    "schema",
    "prov",
    "geo",
    "gr",
    "time",
    "event",
    "void",
    "org"
)

def topLOV(n: Int) = {
    lov.take(n.max(lov.size-1))
}

def getExperimentDef(name: String, discoveries: Seq[String]) = {

    val discoveryList = discoveries.map { d =>
        s"<https://discovery.linkedpipes.com/resource/discovery/$d/config>"
    }.mkString("\n")

    s"""
      |<https://discovery.linkedpipes.com/resource/experiment/$name/config> a <https://discovery.linkedpipes.com/vocabulary/experiment/Experiment> ;
      |    <https://discovery.linkedpipes.com/vocabulary/experiment/hasDiscovery>
      |    (
      |        $discoveryList
      |    ).
    """.stripMargin
}

def getDiscoveryDefs(name: String, transformers: Seq[String]) = s"""
  |   <https://discovery.linkedpipes.com/resource/discovery/$name/config> a <https://discovery.linkedpipes.com/vocabulary/discovery/Input> ;
  |     <https://discovery.linkedpipes.com/vocabulary/discovery/hasTemplate>
  |
  |     #Apps
  |	    <https://discovery.linkedpipes.com/resource/application/timeline/template>,
  |	    <https://discovery.linkedpipes.com/resource/application/timeline-with-labels/template>,
  |	    <https://discovery.linkedpipes.com/resource/application/timeline-periods/template>,
  |	    <https://discovery.linkedpipes.com/resource/application/timeline-periods-with-labels/template>,
  |     <https://discovery.linkedpipes.com/resource/application/map/template>,
  |     <https://discovery.linkedpipes.com/resource/application/map-labeled-points/template>,
  |     <https://discovery.linkedpipes.com/resource/application/dcterms/template>,
  |
  |     #Transformers
  |     ${transformers.mkString("", ",\n", ".")}
  |
  |     #Datasources
  | <https://discovery.linkedpipes.com/resource/discovery/$name/config>
  |    <https://discovery.linkedpipes.com/vocabulary/discovery/hasTemplate>
  |      <https://discovery.linkedpipes.com/resource/lod/templates/http---202.45.139.84-10035-catalogs-fao-repositories-agrovoc> ,
  |	  <https://discovery.linkedpipes.com/resource/lod/templates/http---affymetrix.bio2rdf.org-sparql> ,
  |	  <https://discovery.linkedpipes.com/resource/lod/templates/http---apps.morelab.deusto.es-labman-sparql> ,
  |	  <https://discovery.linkedpipes.com/resource/lod/templates/http---biomodels.bio2rdf.org-sparql> ,
  |	  <https://discovery.linkedpipes.com/resource/lod/templates/http---commons.dbpedia.org-sparql> ,
  |	  <https://discovery.linkedpipes.com/resource/lod/templates/http---cr.eionet.europa.eu-sparql> ,
  |	  <https://discovery.linkedpipes.com/resource/lod/templates/http---crtm.linkeddata.es-sparql> ,
  |	  <https://discovery.linkedpipes.com/resource/lod/templates/http---data.aalto.fi-sparql> ,
  |	  <https://discovery.linkedpipes.com/resource/lod/templates/http---data.allie.dbcls.jp-sparql> ,
  |	  <https://discovery.linkedpipes.com/resource/lod/templates/http---data.hnm.hu-sparql> ,
  |	  <https://discovery.linkedpipes.com/resource/lod/templates/http---data.lenka.no-sparql> ,
  |	  <https://discovery.linkedpipes.com/resource/lod/templates/http---data.linkededucation.org-request-ted-sparql> ,
  |	  <https://discovery.linkedpipes.com/resource/lod/templates/http---data.logainm.ie-sparql> ,
  |	  <https://discovery.linkedpipes.com/resource/lod/templates/http---data.nobelprize.org-sparql> ,
  |	  <https://discovery.linkedpipes.com/resource/lod/templates/http---data.open.ac.uk-query> ,
  |	  <https://discovery.linkedpipes.com/resource/lod/templates/http---data.rism.info-sparql> ,
  |	  <https://discovery.linkedpipes.com/resource/lod/templates/http---data.szepmuveszeti.hu-sparql> ,
  |	  <https://discovery.linkedpipes.com/resource/lod/templates/http---data.utpl.edu.ec-ecuadorresearch-lod-sparql> ,
  |	  <https://discovery.linkedpipes.com/resource/lod/templates/http---data.utpl.edu.ec-utpl-lod-sparql> ,
  |	  <https://discovery.linkedpipes.com/resource/lod/templates/http---dati.isma.roma.it-sparql-> ,
  |	  <https://discovery.linkedpipes.com/resource/lod/templates/http---dati.isprambiente.it-sparql> ,
  |	  <https://discovery.linkedpipes.com/resource/lod/templates/http---de.dbpedia.org-sparql> ,
  |	  <https://discovery.linkedpipes.com/resource/lod/templates/http---edit.elte.hu-sparql> ,
  |	  <https://discovery.linkedpipes.com/resource/lod/templates/http---en.openei.org-sparql> ,
  |	  <https://discovery.linkedpipes.com/resource/lod/templates/http---environment.data.gov.uk-sparql-bwq-query> ,
  |	  <https://discovery.linkedpipes.com/resource/lod/templates/http---es.dbpedia.org-sparql> ,
  |	  <https://discovery.linkedpipes.com/resource/lod/templates/http---eu.dbpedia.org-sparql> ,
  |	  <https://discovery.linkedpipes.com/resource/lod/templates/http---eventmedia.eurecom.fr-sparql> ,
  |	  <https://discovery.linkedpipes.com/resource/lod/templates/http---extbi.lab.aau.dk-sparql-> ,
  |	  <https://discovery.linkedpipes.com/resource/lod/templates/http---farolas.linkeddata.es-sparql> ,
  |	  <https://discovery.linkedpipes.com/resource/lod/templates/http---fr.dbpedia.org-sparql> ,
  |	  <https://discovery.linkedpipes.com/resource/lod/templates/http---genage.bio2rdf.org-sparql> ,
  |	  <https://discovery.linkedpipes.com/resource/lod/templates/http---gendr.bio2rdf.org-sparql> ,
  |	  <https://discovery.linkedpipes.com/resource/lod/templates/http---homologene.bio2rdf.org-sparql> ,
  |	  <https://discovery.linkedpipes.com/resource/lod/templates/http---id.sgcb.mcu.es-sparql> ,
  |	  <https://discovery.linkedpipes.com/resource/lod/templates/http---interpro.bio2rdf.org-sparql> ,
  |	  <https://discovery.linkedpipes.com/resource/lod/templates/http---ja.dbpedia.org-sparql> ,
  |	  <https://discovery.linkedpipes.com/resource/lod/templates/http---kaiko.getalp.org-sparql> ,
  |	  <https://discovery.linkedpipes.com/resource/lod/templates/http---kegg.bio2rdf.org-sparql> ,
  |	  <https://discovery.linkedpipes.com/resource/lod/templates/http---ldf.fi-ww1lod-sparql> ,
  |	  <https://discovery.linkedpipes.com/resource/lod/templates/http---linked.opendata.cz-sparql> ,
  |	  <https://discovery.linkedpipes.com/resource/lod/templates/http---linkeddata.es-resource-ldqm-sparql> ,
  |	  <https://discovery.linkedpipes.com/resource/lod/templates/http---linkedgeodata.org-sparql-> ,
  |	  <https://discovery.linkedpipes.com/resource/lod/templates/http---linkedspending.aksw.org-sparql> ,
  |	  <https://discovery.linkedpipes.com/resource/lod/templates/http---live.dbpedia.org-sparql> ,
  |	  <https://discovery.linkedpipes.com/resource/lod/templates/http---lod.euscreen.eu-sparql> ,
  |	  <https://discovery.linkedpipes.com/resource/lod/templates/http---lod.kaist.ac.kr-sparql> ,
  |	  <https://discovery.linkedpipes.com/resource/lod/templates/http---lod.xdams.org-sparql> ,
  |	  <https://discovery.linkedpipes.com/resource/lod/templates/http---lodstats.aksw.org-sparql> ,
  |	  <https://discovery.linkedpipes.com/resource/lod/templates/http---lsr.bio2rdf.org-sparql> ,
  |	  <https://discovery.linkedpipes.com/resource/lod/templates/http---mesh.bio2rdf.org-sparql> ,
  |	  <https://discovery.linkedpipes.com/resource/lod/templates/http---mgi.bio2rdf.org-sparql> ,
  |	  <https://discovery.linkedpipes.com/resource/lod/templates/http---ncbigene.bio2rdf.org-sparql> ,
  |	  <https://discovery.linkedpipes.com/resource/lod/templates/http---ndc.bio2rdf.org-sparql> ,
  |	  <https://discovery.linkedpipes.com/resource/lod/templates/http---nl.dbpedia.org-sparql> ,
  |	  <https://discovery.linkedpipes.com/resource/lod/templates/http---onto.beef.org.pl-sparql> ,
  |	  <https://discovery.linkedpipes.com/resource/lod/templates/http---opendata.caceres.es-sparql> ,
  |	  <https://discovery.linkedpipes.com/resource/lod/templates/http---opendata.caceres.es-sparql-> ,
  |	  <https://discovery.linkedpipes.com/resource/lod/templates/http---opendatacommunities.org-sparql> ,
  |	  <https://discovery.linkedpipes.com/resource/lod/templates/http---orphanet.bio2rdf.org-sparql> ,
  |	  <https://discovery.linkedpipes.com/resource/lod/templates/http---premon.fbk.eu-sparql> ,
  |	  <https://discovery.linkedpipes.com/resource/lod/templates/http---pt.dbpedia.org-sparql> ,
  |	  <https://discovery.linkedpipes.com/resource/lod/templates/http---pubmed.bio2rdf.org-sparql> ,
  |	  <https://discovery.linkedpipes.com/resource/lod/templates/http---rdf.disgenet.org-sparql-> ,
  |	  <https://discovery.linkedpipes.com/resource/lod/templates/http---semantic.eea.europa.eu-sparql> ,
  |	  <https://discovery.linkedpipes.com/resource/lod/templates/http---serendipity.utpl.edu.ec-lod-sparql> ,
  |	  <https://discovery.linkedpipes.com/resource/lod/templates/http---sider.bio2rdf.org-sparql> ,
  |	  <https://discovery.linkedpipes.com/resource/lod/templates/http---sparql.odw.tw> ,
  |	  <https://discovery.linkedpipes.com/resource/lod/templates/http---sparql.orthodb.org-sparql> ,
  |	  <https://discovery.linkedpipes.com/resource/lod/templates/http---statistics.data.gov.uk-sparql> ,
  |	  <https://discovery.linkedpipes.com/resource/lod/templates/http---statistics.gov.scot-sparql> ,
  |	  <https://discovery.linkedpipes.com/resource/lod/templates/http---taxonomy.bio2rdf.org-sparql> ,
  |	  <https://discovery.linkedpipes.com/resource/lod/templates/http---vocabularies.unesco.org-sparql> ,
  |	  <https://discovery.linkedpipes.com/resource/lod/templates/http---webenemasuno.linkeddata.es-sparql> ,
  |	  <https://discovery.linkedpipes.com/resource/lod/templates/http---wikidata.dbpedia.org-sparql> ,
  |	  <https://discovery.linkedpipes.com/resource/lod/templates/http---www.imagesnippets.com-sparql-images> ,
  |	  <https://discovery.linkedpipes.com/resource/lod/templates/http---www.linklion.org-8890-sparql> ,
  |	  <https://discovery.linkedpipes.com/resource/lod/templates/http---www.lotico.com-3030-lotico-sparql> ,
  |	  <https://discovery.linkedpipes.com/resource/lod/templates/http---www.rechercheisidore.fr-sparql> ,
  |	  <https://discovery.linkedpipes.com/resource/lod/templates/http---zbw.eu-beta-sparql-stw-query> ,
  |	  <https://discovery.linkedpipes.com/resource/lod/templates/https---data.cssz.cz-sparql> ,
  |	  <https://discovery.linkedpipes.com/resource/lod/templates/https---linked.opendata.cz-sparql> ,
  |	  <https://discovery.linkedpipes.com/resource/lod/templates/https---nkod.opendata.cz-sparql> ,
  |	  <https://discovery.linkedpipes.com/resource/lod/templates/https---ruian.linked.opendata.cz-sparql> ,
  |	  <https://discovery.linkedpipes.com/resource/lod/templates/https---w3id.org-scholarlydata-sparql> ,
  |	  <https://discovery.linkedpipes.com/resource/lod/templates/https---www.europeandataportal.eu-sparql> .
""".stripMargin

val transformersData = Source.fromFile("transformers.json").getLines().mkString("\n")
val transformers = Json.parse(transformersData)

def fact(n: Int) : Int = (1 to n).product
def combs(n: Int, k: Int) = fact(n)/(fact(k)*fact(n-k))


/*
def experiment1 = {
    val usedTransformers = (transformers \ "labels" \ "external").as[Seq[String]]


    for {
        len <- 1 to usedTransformers.size
        combinations <- usedTransformers combinations len
    } yield {
        combinations
        //getDiscoveryDefs("01-labels-external", combinations)
    }
}*/

private def sortByLov(transformerUris: Seq[String]) = {
    transformerUris.sortBy(t => lov.indexOf(lov.find(prefix => t.contains(s"/$prefix-")).head))
}

def experimentLabelsLov() = {
    val usedTransformers = (transformers \ "labels" \ "external").as[Seq[String]]
    val sortedTransformers = sortByLov(usedTransformers)
    val experimentName = "001-labels-external-no-groups"

    def getDiscoveryName(i: Int) = s"$experimentName-${"%03d".format(i)}"

    val discoveryDefs = for {
        len <- 1 to sortedTransformers.size
    } yield {
        val discoveryName = getDiscoveryName(len)
        (discoveryName, getDiscoveryDefs(discoveryName, sortedTransformers.take(len)))
    }

    val experimentDef = getExperimentDef(experimentName, 1.to(sortedTransformers.size).map(i => getDiscoveryName(i)))
    (experimentName, experimentDef, discoveryDefs)
}

val experimentDefs = Seq(
    experimentLabelsLov()
)

val basePath = "/Users/jirihelmich/dev/mff/discovery/data/rdf/discovery-input"

experimentDefs.foreach { case (experimentName, experimentDef, discoveryDefs) =>
    s"$basePath/experiment/$experimentName/config.ttl".toFile.createFileIfNotExists(true).writeText(experimentDef)

    for {
        (name, definition) <- discoveryDefs
    } {
        s"$basePath/discovery/$name/config.ttl".toFile.createFileIfNotExists(true).writeText(definition)
    }
}