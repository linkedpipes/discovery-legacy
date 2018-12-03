package services.discovery

import services.discovery.model.Pipeline
import services.discovery.model.components.ComponentInstanceWithInputs

case class CombinatorInput(fragments: Seq[Pipeline], components: Seq[ComponentInstanceWithInputs])
