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
    "org",
    "geosparql", //?
    "wikidata" //?
)

case class TransformerDef(iri: String, isDirect: Boolean = false, predecessors : Seq[String] = Seq())

val allTransformerDefs = Seq(
    TransformerDef(
        iri = "https://discovery.linkedpipes.com/resource/transformer/dce-to-dcterms-title/template",
        isDirect = true
    ),
    TransformerDef(
        iri = "https://discovery.linkedpipes.com/resource/transformer/dce-to-dcterms-date/template",
        isDirect = true
    ),
    TransformerDef(
        iri = "https://discovery.linkedpipes.com/resource/transformer/foaf-name-to-dcterms-title/template",
        isDirect = true
    ),
    TransformerDef(
        iri = "https://discovery.linkedpipes.com/resource/transformer/skos-preflabel-to-dcterms-title/template",
        isDirect = true
    ),
    TransformerDef(
        iri = "https://discovery.linkedpipes.com/resource/transformer/schema-name-to-dcterms-title/template",
        isDirect = true
    ),
    TransformerDef(
        iri = "https://discovery.linkedpipes.com/resource/transformer/schema-enddate-to-dcterms-date/template",
        isDirect = true
    ),
    TransformerDef(
        iri = "https://discovery.linkedpipes.com/resource/transformer/schema-startdate-to-dcterms-date/template",
        isDirect = true
    ),
    TransformerDef(
        iri = "https://ldcp.opendata.cz/resource/schema/transformer/schema-bookingTime-to-time-inXSDDateTimeStamp/template"
    ),
    TransformerDef(
        iri = "https://ldcp.opendata.cz/resource/schema/transformer/schema-commentTime-to-time-inXSDDateTimeStamp/template"
    ),
    TransformerDef(
        iri = "https://ldcp.opendata.cz/resource/schema/transformer/schema-datasetTimeInterval-to-time-inXSDDateTimeStamp/template"
    ),
    TransformerDef(
        iri = "https://ldcp.opendata.cz/resource/schema/transformer/schema-dateIssued-to-time-inXSDDateTimeStamp/template"
    ),
    TransformerDef(
        iri = "https://ldcp.opendata.cz/resource/schema/transformer/schema-dateModified-to-time-inXSDDateTimeStamp/template"
    ),
    TransformerDef(
        iri = "https://ldcp.opendata.cz/resource/schema/transformer/schema-datePosted-to-time-inXSDDateTimeStamp/template"
    ),
    TransformerDef(
        iri = "https://ldcp.opendata.cz/resource/schema/transformer/schema-datePublished-to-time-inXSDDateTimeStamp/template"
    ),
    TransformerDef(
        iri = "https://ldcp.opendata.cz/resource/schema/transformer/schema-dateRead-to-time-inXSDDateTimeStamp/template"
    ),
    TransformerDef(
        iri = "https://ldcp.opendata.cz/resource/schema/transformer/schema-dateReceived-to-time-inXSDDateTimeStamp/template"
    ),
    TransformerDef(
        iri = "https://ldcp.opendata.cz/resource/schema/transformer/schema-dateSent-to-time-inXSDDateTimeStamp/template"
    ),
    TransformerDef(
        iri = "https://ldcp.opendata.cz/resource/schema/transformer/schema-dateVehicleFirstRegistered-to-time-inXSDDateTimeStamp/template"
    ),
    TransformerDef(
        iri = "https://ldcp.opendata.cz/resource/schema/transformer/schema-dissolutionDate-to-time-inXSDDateTimeStamp/template"
    ),
    TransformerDef(
        iri = "https://ldcp.opendata.cz/resource/schema/transformer/schema-doorTime-to-time-inXSDDateTimeStamp/template"
    ),
    TransformerDef(
        iri = "https://ldcp.opendata.cz/resource/schema/transformer/schema-dropoffTime-to-time-inXSDDateTimeStamp/template"
    ),
    TransformerDef(
        iri = "https://ldcp.opendata.cz/resource/schema/transformer/schema-foundingDate-to-time-inXSDDateTimeStamp/template"
    ),
    TransformerDef(
        iri = "https://ldcp.opendata.cz/resource/schema/transformer/schema-lastReviewed-to-time-inXSDDateTimeStamp/template"
    ),
    TransformerDef(
        iri = "https://ldcp.opendata.cz/resource/schema/transformer/schema-modifiedTime-to-time-inXSDDateTimeStamp/template"
    ),
    TransformerDef(
        iri = "https://ldcp.opendata.cz/resource/schema/transformer/schema-orderDate-to-time-inXSDDateTimeStamp/template"
    ),
    TransformerDef(
        iri = "https://ldcp.opendata.cz/resource/schema/transformer/schema-paymentDue-to-time-inXSDDateTimeStamp/template"
    ),
    TransformerDef(
        iri = "https://ldcp.opendata.cz/resource/schema/transformer/schema-paymentDueDate-to-time-inXSDDateTimeStamp/template"
    ),
    TransformerDef(
        iri = "https://ldcp.opendata.cz/resource/schema/transformer/schema-pickupTime-to-time-inXSDDateTimeStamp/template"
    ),
    TransformerDef(
        iri = "https://ldcp.opendata.cz/resource/schema/transformer/schema-previousStartDate-to-time-inXSDDateTimeStamp/template"
    ),
    TransformerDef(
        iri = "https://ldcp.opendata.cz/resource/schema/transformer/schema-priceValidUntil-to-time-inXSDDateTimeStamp/template"
    ),
    TransformerDef(
        iri = "https://ldcp.opendata.cz/resource/schema/transformer/schema-productionDate-to-time-inXSDDateTimeStamp/template"
    ),
    TransformerDef(
        iri = "https://ldcp.opendata.cz/resource/schema/transformer/schema-purchaseDate-to-time-inXSDDateTimeStamp/template"
    ),
    TransformerDef(
        iri = "https://ldcp.opendata.cz/resource/schema/transformer/schema-releaseDate-to-time-inXSDDateTimeStamp/template"
    ),
    TransformerDef(
        iri = "https://ldcp.opendata.cz/resource/schema/transformer/schema-scheduledPaymentDate-to-time-inXSDDateTimeStamp/template"
    ),
    TransformerDef(
        iri = "https://ldcp.opendata.cz/resource/schema/transformer/schema-scheduledTime-to-time-inXSDDateTimeStamp/template"
    ),
    TransformerDef(
        iri = "https://ldcp.opendata.cz/resource/schema/transformer/schema-temporal-to-time-inXSDDateTimeStamp/template"
    ),
    TransformerDef(
        iri = "https://ldcp.opendata.cz/resource/schema/transformer/schema-temporalCoverage-to-time-inXSDDateTimeStamp/template"
    ),
    TransformerDef(
        iri = "https://ldcp.opendata.cz/resource/schema/transformer/schema-uploadDate-to-time-inXSDDateTimeStamp/template"
    ),
    TransformerDef(
        iri = "https://ldcp.opendata.cz/resource/schema/transformer/schema-vehicleModelDate-to-time-inXSDDateTimeStamp/template"
    ),
    TransformerDef(
        iri = "https://ldcp.opendata.cz/resource/schema/transformer/schema-webCheckinTime-to-time-inXSDDateTimeStamp/template"
    ),
    TransformerDef(
        iri = "https://discovery.linkedpipes.com/resource/transformer/prov-attime-to-dcterms-date/template"
    ),
    TransformerDef(
        iri = "https://discovery.linkedpipes.com/resource/transformer/geo-pos-to-schema-geocoordinates/template",
        isDirect = true
    ),
    TransformerDef(
        iri = "https://discovery.linkedpipes.com/resource/transformer/gr-name-to-dcterms-title/template",
        isDirect = true
    ),
    TransformerDef(
        iri = "https://discovery.linkedpipes.com/resource/transformer/time-inxsddatetimestamp-to-dcterms-date/template",
        isDirect = true
    ),
    TransformerDef(
        iri = "https://discovery.linkedpipes.com/resource/transformer/time-inxsddate-to-dcterms-date/template"
    ),
    TransformerDef(
        iri = "https://discovery.linkedpipes.com/resource/transformer/geosparql-aswkt-to-schema-geocoordinates/template",
        isDirect = true
    ),
    TransformerDef(
        iri = "https://discovery.linkedpipes.com/resource/transformer/wikidata-coordinate-location-to-schema-geocoordinates/template"
    )
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

def getDiscoveryDefs(name: String, transformers: Seq[String], groups: Map[String, Seq[String]] = Map()) = {

    val nonEmptyGroups = groups.filterNot(g => g._2.isEmpty)

    val transformerGroups = {
        nonEmptyGroups.map { case (prefix, trans) =>
            s"<https://discovery.linkedpipes.com/resource/transformer-group/$name-$prefix/label>"
        }
    }

    val transformerGroupsContent = {
        nonEmptyGroups.map { case (prefix, trans) =>

            s"""
              |<https://discovery.linkedpipes.com/resource/transformer-group/$name-$prefix/label> a <https://discovery.linkedpipes.com/vocabulary/discovery/TransformerGroup> ;
              |    <https://discovery.linkedpipes.com/vocabulary/discovery/hasTransformer>
              |      ${trans.mkString(",\n")}
              |    .
            """.stripMargin
        }.mkString("\n")
    }

    def printIfNonEmpty(uri: String, list: Iterable[String]): String =
    {
        if (list.isEmpty) {
            ""
        } else {
            list.mkString(s"$uri ", ",\n", ";")
        }
    }

    s"""
       |   <https://discovery.linkedpipes.com/resource/discovery/$name/config> a <https://discovery.linkedpipes.com/vocabulary/discovery/Input> ;
       |
       |     #Transformers
       |     ${printIfNonEmpty("<https://discovery.linkedpipes.com/vocabulary/discovery/hasTemplate>", transformers)}
       |
       |     #Transformer groups
       |     ${printIfNonEmpty("<https://discovery.linkedpipes.com/vocabulary/discovery/hasTransformerGroup>", transformerGroups)}
       |
       |     <https://discovery.linkedpipes.com/vocabulary/discovery/hasTemplate>
       |
       |     #Apps
       |	    <https://discovery.linkedpipes.com/resource/application/timeline/template>,
       |	    <https://discovery.linkedpipes.com/resource/application/timeline-with-labels/template>,
       |	    <https://discovery.linkedpipes.com/resource/application/timeline-periods/template>,
       |	    <https://discovery.linkedpipes.com/resource/application/timeline-periods-with-labels/template>,
       |     <https://discovery.linkedpipes.com/resource/application/map/template>,
       |     <https://discovery.linkedpipes.com/resource/application/map-labeled-points/template>,
       |     <https://discovery.linkedpipes.com/resource/application/dcterms/template> .
       |
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
       |
       |   $transformerGroupsContent
    """.stripMargin
}

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

val transformersData = Source.fromFile("gen/transformers.json").getLines().mkString("\n")
val transformers = Json.parse(transformersData)

private def getDiscoveryName(experimentName: String, i: Int) = s"$experimentName-${"%03d".format(i)}"

private def getSortedLovLabels = {
    def sortByLov(transformerUris: Seq[String]) = {
        transformerUris.sortBy(t => lov.indexOf(lov.find(prefix => t.contains(s"/$prefix-")).head))
    }

    val allTransformers = Seq(
        transformers \ "labels" \ "external",
        transformers \ "labels" \ "internal",
        transformers \ "date-instants" \ "external",
        transformers \ "date-instants" \ "internal",
        transformers \ "time-instants" \ "external",
        transformers \ "time-intervals" \ "external",
        transformers \ "time-intervals" \ "internal",
        transformers \ "geo" \ "external",
        transformers \ "geo" \ "internal"
    ).flatMap(l => l.as[Seq[String]])

    sortByLov(allTransformers)
}

def experimentLovGroupBy(experimentName: String, groupByFunc: Seq[String] => Map[String, Seq[String]]) = {
    val sortedTransformers = getSortedLovLabels

    val start = 0

    val discoveryDefs = for {
        len <- start to sortedTransformers.size
    } yield {
        val relevantTransformers = sortedTransformers.take(len)
        val transformerGroups = groupByFunc(relevantTransformers)
        val discoveryName = getDiscoveryName(experimentName, len)
        (
            discoveryName,
            getDiscoveryDefs(discoveryName, relevantTransformers, transformerGroups)
        )
    }

    val experimentDef = getExperimentDef(
        experimentName,
        start.to(sortedTransformers.size).map(i => getDiscoveryName(experimentName, i))
    )
    (experimentName, experimentDef, discoveryDefs)
}

def groupByVocabulary(transformers: Seq[String], patternGetter: String => Seq[String]) : Map[String, Seq[String]] = {
    lov.map { prefix =>
        (prefix, transformers.filter { t => patternGetter(prefix).forall(p => t.contains(p)) })
    }.toMap
}

def groupByTargetVocabulary(transformers: Seq[String]) = groupByVocabulary(transformers, prefix => Seq(s"-to-$prefix"))
def groupBySourceVocabulary(transformers: Seq[String]) = groupByVocabulary(transformers, prefix => Seq(s"/$prefix-"))

def groupBySourceAndTargetVocabulary(transformers: Seq[String]): Map[String, Seq[String]] = {
    transformers.map { t =>
        val part = t.split("/").dropRight(1).last
        val fromto = part.split("-to-")
        s"${fromto(0).split("-").head}-${fromto(1).split("-").head}" -> t
    }.groupBy(_._1).mapValues(_.map(_._2))
}

def groupByDomainAndVocabulary(transformers: Seq[String], direction: String, f: Seq[String] => Map[String, Seq[String]]): Map[String, Seq[String]] = {
    val domainGrouping = transformers.groupBy {
        case t1 if t1.contains("schema-geocoordinates") => "geo"
        case t2 if t2.contains("dcterms-title") => "labels"
        case _ => "datetime"
    }
    val deepGrouping = domainGrouping.map { case (key, group) => key -> f(group) }

    deepGrouping.flatMap { case (domain, domainGroup) =>
        domainGroup.map { case (prefix, prefixGroup) =>
            s"$domain-$direction-$prefix" -> prefixGroup
        }
    }
}

def groupByDomainAndSourceVocabulary(transformers: Seq[String]): Map[String, Seq[String]] = {
    groupByDomainAndVocabulary(transformers, "source", groupBySourceVocabulary)
}

def groupByDomainAndTargetVocabulary(transformers: Seq[String]): Map[String, Seq[String]] = {
    groupByDomainAndVocabulary(transformers, "target", groupByTargetVocabulary)
}

def groupByTargetProperty(transformers: Seq[String]): Map[String, Seq[String]] = {
    transformers.groupBy { t =>
        t.split("-to-").last.replace("/template>", "")
    }
}

val experimentDefs = Seq(
    experimentLovGroupBy("001-no-groups", _ => Map())
    //experimentLovGroupBy("002-target-voc-groups", groupByTargetVocabulary),
    //experimentLovGroupBy("003-source-voc-groups", groupBySourceVocabulary),
    //experimentLovGroupBy("004-source-target-voc-groups", groupBySourceAndTargetVocabulary),
    //experimentLovGroupBy("005-domain-source-voc-groups", groupByDomainAndSourceVocabulary),
    //experimentLovGroupBy("006-domain-target-voc-groups", groupByDomainAndTargetVocabulary),
    //experimentLovGroupBy("007-target-prop-groups", groupByTargetProperty)
)

val basePath = "/Users/jirihelmich/dev/mff/discovery/data/rdf/discovery-input"

experimentDefs.foreach { case (experimentName, experimentDef, discoveryDefs) =>
    s"$basePath/experiment/$experimentName/config.ttl".toFile.createFileIfNotExists(createParents = true).writeText(experimentDef)

    for {
        (name, definition) <- discoveryDefs
    } {
        s"$basePath/discovery/$name/config.ttl".toFile.createFileIfNotExists(createParents = true).writeText(definition)
    }
}