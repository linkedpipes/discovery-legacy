package services

import java.io.{ByteArrayInputStream, ByteArrayOutputStream}
import java.net.URLEncoder

import better.files.File
import controllers.dto.{SparqlEndpointDefinition, SparqlEndpointGraph}
import org.apache.jena.atlas.web.HttpException
import org.apache.jena.query.Dataset
import org.apache.jena.rdf.model.{Model, ModelFactory}
import org.apache.jena.riot.{Lang, RDFDataMgr, RiotException}
import play.Logger.ALogger
import scalaj.http.Http
import services.vocabulary.SD

import scala.collection.JavaConverters._

object RdfUtils {

    def modelFromTtl(ttlData: String): Model = {
        val model = ModelFactory.createDefaultModel()
        model.read(new ByteArrayInputStream(ttlData.getBytes("UTF-8")), null, "TTL")
    }

    def modelFromTtl(f: File) : Model = {
        val model = ModelFactory.createDefaultModel()
        val reader = f.newFileReader
        model.read(reader, null, "TTL")
        reader.close()
        model
    }

    def modelToTtl(model: Model) = {
        val outputStream = new ByteArrayOutputStream()
        RDFDataMgr.write(outputStream, model, Lang.TTL)
        outputStream.toString()
    }

    def fromJsonLd[R](iri: String)(fn: Dataset => R): R = {
        fn(RDFDataMgr.loadDataset(iri, Lang.JSONLD))
    }

    def modelFromIri[R](iri: String)(discoveryLogger: ALogger)(fn: Either[Throwable, Model] => R): R = {
        discoveryLogger.debug(s"Downloading data from $iri.")
        val result = try {
            val model = ModelFactory.createDefaultModel()
            model.read(iri)
            Right(model)
        } catch {
            case e: RiotException => Left(new Exception(s"The data at $iri caused the following error: ${e.getMessage}."))
            case e: HttpException => Left(new Exception(s"The data at $iri caused the following error: ${e.getMessage}."))
        }
        fn(result)
    }

    def readServiceDescription[R](iri: String)(discoveryLogger: ALogger)(fn: Either[Throwable, SparqlEndpointGraph] => R): R = {
        fn(
            modelFromIri(iri)(discoveryLogger) {
                case Right(model) => {
                    val endpointUri = model.listObjectsOfProperty(SD.endpoint).toList.asScala.head.asResource().getURI
                    val graphIri = model.listObjectsOfProperty(SD.namedGraph).toList.asScala.head.asResource().getPropertyResourceValue(SD.name).asResource().getURI
                    Right(SparqlEndpointGraph(SparqlEndpointDefinition("VIRTUOSO", endpointUri), graphIri))
                }
            }
        )
    }

    def graphStoreBlazegraph(dataSample: Model, sparqlEndpointGraph: SparqlEndpointGraph) = {
        val encodedGraphUri = URLEncoder.encode(sparqlEndpointGraph.iri, "UTF-8")
        val params = s"context-uri=$encodedGraphUri"
        Http(s"${sparqlEndpointGraph.endpoint.endpointUri}/sparql?$params")
            .postData(RdfUtils.modelToTtl(dataSample))
            .header("Content-Type", "text/turtle")
            .asString
    }

}
