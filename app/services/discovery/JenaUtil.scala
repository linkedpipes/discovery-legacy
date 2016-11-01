package services.discovery

import java.net.URL

import org.apache.jena.rdf.model.{Model, ModelFactory}
import com.typesafe.scalalogging.StrictLogging
import org.apache.commons.io.IOUtils
import resource._

object JenaUtil extends StrictLogging{
  private val defaultEncoding = "UTF-8"

  def modelFromTtl(ttl: String): Model = {
    // scalastyle:off null
    ModelFactory.createDefaultModel().read(IOUtils.toInputStream(ttl, defaultEncoding), null, "TTL")
    // scalastyle:on null
  }

  def modelFromTtlFile(url: URL): Model = {
    val model = ModelFactory.createDefaultModel()
    for (
      source <- managed(scala.io.Source.fromURL(url, defaultEncoding));
      reader <- managed(source.reader())
    ) {
      // scalastyle:off null
      logger.info(url.toString)
      model.read(reader, null, "TTL")
      // scalastyle:on null
    }
    model
  }

}
