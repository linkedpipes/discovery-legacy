package services.discovery.model

import java.util.UUID

class EtlIriGenerator {

  private val prefix = "http://demo.etl.linkedpipes.com/resources/pipelines/"

  val pipelineIri = prefix + GuidGenerator.next

  def componentIri = pipelineIri + "/components/" + GuidGenerator.next

  def configurationIri(componentIri: String) = componentIri + "/configuration"

  def connectionIri = pipelineIri + "/connection/" + GuidGenerator.next

}

object GuidGenerator {
  def next = UUID.randomUUID().toString

  def nextIri = "https://linked.opendata.cz/resource/ldcp/result/graph/" + next
}