package services.discovery.components.transformer

import java.util.UUID

import services.discovery.components.common.DescriptorChecker
import services.discovery.model._
import services.discovery.model.components.{AskQuery, SparqlQuery, SparqlTransformerInstance, UpdateQuery}

import scala.concurrent.Future

class Ruian2SchemaOrgTransformer extends SparqlTransformerInstance with DescriptorChecker {
  val portName = "INPUT"
  val port = Port(portName, 0)
  private val query = UpdateQuery(
    """
      | PREFIX s: <http://schema.org/>
      | PREFIX dbo: <http://dbpedia.org/ontology/>
      | PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
      | PREFIX ruian: <http://ruian.linked.opendata.cz/ontology/>
      | PREFIX ogcgml:  <http://www.opengis.net/ont/gml#>
      |
      | DELETE {
      |  ?p ruian:definicniBod ?definicniBod .
      |
      |  ?definicniBod rdf:type ogcgml:MultiPoint ;
      |     ogcgml:pointMember ?pointMember .
      |
      |  ?pointMember rdf:type ogcgml:Point ;
      |     s:geo ?geo .
      |  ?geo rdf:type s:GeoCoordinates ;
      |     s:longitude ?lng ;
      |     s:latitude ?lat .
      | }
      | INSERT {
      |   ?p s:geo [
      |       rdf:type s:GeoCoordinates ;
      |       s:longitude ?lng ;
      |       s:latitude  ?lat
      |     ] .
      | }
      | WHERE {
      |  ?p a dbo:PopulatedPlace ;
      |     ruian:definicniBod ?definicniBod .
      |
      |  ?definicniBod rdf:type ogcgml:MultiPoint ;
      |     ogcgml:pointMember ?pointMember .
      |
      |  ?pointMember rdf:type ogcgml:Point ;
      |     s:geo ?geo .
      |  ?geo rdf:type s:GeoCoordinates ;
      |     s:longitude ?lng ;
      |     s:latitude ?lat .
      | }
    """.stripMargin)

  private val descriptor = AskQuery(
    """
      |PREFIX s: <http://schema.org/>
      |PREFIX dbo: <http://dbpedia.org/ontology/>
      |prefix ogcgml:  <http://www.opengis.net/ont/gml#>
      |prefix ruian: <http://ruian.linked.opendata.cz/ontology/>
      |prefix ruianlink: <http://ruian.linked.opendata.cz/ontology/links/>
      |PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
      |PREFIX owl: <http://www.w3.org/2002/07/owl#>
      |
      |    ASK {
      |      ?p a dbo:PopulatedPlace ;
      |         dbo:populationTotal ?population ;
      |         ruian:definicniBod  ?definicniBod .
      |
      |      ?definicniBod rdf:type  ogcgml:MultiPoint ;
      |         ogcgml:pointMember  ?pointMember .
      |
      |      ?pointMember rdf:type ogcgml:Point ;
      |         s:geo ?geo .
      |      ?geo  rdf:type  s:GeoCoordinates ;
      |         s:longitude ?lng ;
      |         s:latitude  ?lat .
      |    }
    """.stripMargin
  )

  override def checkPort(port: Port, state: Option[ComponentState], outputDataSample: DataSample, discoveryId: UUID, iterationNumber: Int): Future[PortCheckResult] = {
    checkStatelessDescriptors(outputDataSample, discoveryId, iterationNumber, descriptor)
  }

  override def getInputPorts: Seq[Port] = Seq(port)

  override def getOutputDataSample(state: Option[ComponentState], dataSamples: Map[Port, DataSample], discoveryId: UUID, iterationNumber: Int): Future[DataSample] = {
    val newSample = dataSamples(port).transform(query)
    Future.successful(ModelDataSample(newSample))
  }

  override def getQueryByPort(port: Port): SparqlQuery = query
}
