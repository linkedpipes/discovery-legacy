package services.vocabulary

import org.apache.jena.rdf.model.ResourceFactory

trait RdfVocabulary {
  def prefix: String

  protected def resource(name: String) = ResourceFactory.createResource(s"$prefix$name")

  protected def property(name: String) = ResourceFactory.createProperty(s"$prefix$name")
}
