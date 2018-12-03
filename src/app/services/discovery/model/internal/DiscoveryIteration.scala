package services.discovery.model.internal

import java.util.UUID

import services.discovery.model.{DiscoveryInput, Pipeline}

case class DiscoveryIteration(
                                 id: UUID,
                                 fragments: Seq[Pipeline],
                                 pipelines: Seq[Pipeline],
                                 input: DiscoveryInput,
                                 number: Int
) {
    def discoveredNewPipeline(previous: DiscoveryIteration) : Boolean = {
        fragments.lengthCompare(previous.fragments.size) > 0
    }
}
