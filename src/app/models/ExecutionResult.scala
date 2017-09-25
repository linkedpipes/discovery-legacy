package models

import java.util.UUID

case class ExecutionResult(id: UUID, discoveryId: String, pipelineId: String, graphIri: String)