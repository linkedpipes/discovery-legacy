package services.discovery.model.internal

import java.util.UUID

import services.discovery.model.{DiscoveryInput, Pipeline}

case class FragmentList(base: Seq[Pipeline], fresh: Seq[Pipeline], largeDatasets: Seq[Pipeline]) {
    def ordinary = base ++ fresh
}

case class DiscoveryIteration(
    id: UUID,
    fragments: FragmentList,
    pipelines: Seq[Pipeline],
    input: DiscoveryInput,
    number: Int
)