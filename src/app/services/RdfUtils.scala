package services

import java.io.ByteArrayInputStream
import java.net.URLEncoder

import org.apache.jena.atlas.web.HttpException
import org.apache.jena.query.Dataset
import org.apache.jena.rdf.model.{Model, ModelFactory}
import org.apache.jena.riot.{Lang, RDFDataMgr, RiotException}
import play.Logger.ALogger
import scalaj.http.Http

object RdfUtils {

  def modelFromTtl(ttlData: String) = {
    val model = ModelFactory.createDefaultModel()
    model.read(new ByteArrayInputStream(ttlData.getBytes("UTF-8")), null, "TTL")
  }

  def fromJsonLd[R](iri: String)(fn: Dataset => R): R = {
    fn(RDFDataMgr.loadDataset(iri, Lang.JSONLD))
  }

  def modelFromIri[R](uri: String)(discoveryLogger: ALogger)(fn: Either[Throwable, Model] => R): R = {
    discoveryLogger.debug(s"Downloading data from $uri.")
    val result = try {
      val model = ModelFactory.createDefaultModel()
      model.read(uri)
      Right(model)
    } catch {
      case e: RiotException => Left(new Exception(s"The data at $uri caused the following error: ${e.getMessage}."))
      case e: HttpException => Left(new Exception(s"The data at $uri caused the following error: ${e.getMessage}."))
    }
    fn(result)
  }

  def graphStoreBlazegraph(dataSample: String, endpointUri: String, graphUri: String) = {
    val encodedGraphUri = URLEncoder.encode(graphUri, "UTF-8")
    val params = s"context-uri=$encodedGraphUri"
    val response = Http(s"$endpointUri/sparql?$params").postData(dataSample).header("Content-Type", "text/turtle").asString
  }

}
