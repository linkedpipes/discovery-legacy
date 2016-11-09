package services.discovery.components.analyzer

import java.util.UUID

import services.discovery.components.common.DescriptorChecker
import services.discovery.model._
import services.discovery.model.components.{AnalyzerInstance, AskQuery}

import scala.concurrent.Future

class RuianGeocoderAnalyzer extends AnalyzerInstance with DescriptorChecker {
  val linkPortName: String = "PORT_LINK"
  val geoPortName: String = "PORT_GEO"

  override def getOutputDataSample(state: Option[ComponentState], dataSamples: Map[Port, DataSample], discoveryId: UUID, iterationNumber: Int): Future[DataSample] = {
    Future.successful(DataSample.apply(
      """
        | PREFIX s: <http://schema.org/>
        |
        | <http://example.com/entity/geo> s:geo [
        |   a s:GeoCoordinates ;
        |   s:latitude 15.13 ;
        |   s:longitude 50.72
        | ] .
      """.stripMargin
    ))
  }

  private val geoPortDescriptor = AskQuery(
    """
      |   prefix xsd: <http://www.w3.org/2001/XMLSchema#>
      |   prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
      |   prefix skos:  <http://www.w3.org/2004/02/skos/core#>
      |   prefix s: <http://schema.org/>
      |   prefix ogcgml:  <http://www.opengis.net/ont/gml#>
      |   prefix ruian: <http://ruian.linked.opendata.cz/ontology/>
      |   ASK {
      |   ?object
      |     ruian:definicniBod  ?definicniBod .
      |
      |   ?definicniBod rdf:type  ogcgml:MultiPoint ;
      |     ogcgml:pointMember  ?pointMember .
      |
      |   ?pointMember rdf:type ogcgml:Point ;
      |     s:geo ?geo .
      |   ?geo  rdf:type  s:GeoCoordinates ;
      |     s:longitude ?lng ;
      |     s:latitude  ?lat .
      |   }
    """.stripMargin
  )

  private val linkPortDescriptor = AskQuery(
    """
      |   prefix ruianlink: <http://ruian.linked.opendata.cz/ontology/links/>
      |   ASK {
      |     ?object ruianlink:obec  ?obec .
      |   }
    """.stripMargin
  )

  override def checkPort(port: Port, state: Option[ComponentState], outputDataSample: DataSample, discoveryId: UUID, iterationNumber: Int): Future[PortCheckResult] = port match {
    case Port(`linkPortName`, _) => checkStatelessDescriptors(outputDataSample, discoveryId, iterationNumber, linkPortDescriptor)
    case Port(`geoPortName`, _) => checkStatelessDescriptors(outputDataSample, discoveryId, iterationNumber, geoPortDescriptor)
  }

  override val getInputPorts: Seq[Port] = Seq(Port(linkPortName, 0), Port(geoPortName, 0))
}
