package services

import java.util.UUID

import controllers.dto.DiscoveryResult
import services.discovery.Discovery
import services.discovery.model.etl.{EtlPipeline, EtlPipelineExporter}
import services.discovery.model.{DiscoveryInput, Pipeline}

import scala.collection.mutable


class DiscoveryService {

    private val discoveries = new scala.collection.mutable.HashMap[UUID, Discovery]

    def start(input: DiscoveryInput) = {
        val discovery = Discovery.create
        discoveries.put(discovery.id, discovery)
        discovery.discover(input)
        discovery.id
    }

    def stop(id: String) = {}

    def getStatus(id: String): Option[DiscoveryResult] = withDiscovery(id) { discovery =>
        DiscoveryResult(
            discovery.results.size,
            discovery.isFinished,
            (discovery.end - discovery.start) / (1000 * 1000) // ns -> ms
        )
    }

    def getEtlPipeline(id: String, pipelineId: String): Option[EtlPipeline] = withDiscovery(id) { discovery =>
        EtlPipelineExporter.export(discovery.results.get(UUID.fromString(pipelineId)).toSeq).headOption
    }.flatten

    def getPipelines(id: String): Option[mutable.HashMap[UUID, Pipeline]] = withDiscovery(id) { discovery =>
        discovery.results
    }

    private def withDiscovery[R](id: String)(action: Discovery => R): Option[R] = {
        val uuid = UUID.fromString(id)
        discoveries.get(uuid).map(action)
    }

}
