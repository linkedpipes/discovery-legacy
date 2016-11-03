package services.discovery.model.internal

import java.util.UUID

import services.discovery.model.{DiscoveryInput, Pipeline}

case class IterationData(
    id: UUID,
    givenPipelines: Seq[Pipeline],
    completedPipelines: Seq[Pipeline],
    availableComponents: DiscoveryInput,
    iterationNumber: Int
)
