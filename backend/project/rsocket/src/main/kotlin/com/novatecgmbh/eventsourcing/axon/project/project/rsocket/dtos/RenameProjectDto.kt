package com.novatecgmbh.eventsourcing.axon.project.project.rsocket.dtos

import com.novatecgmbh.eventsourcing.axon.project.project.api.ProjectId
import com.novatecgmbh.eventsourcing.axon.project.project.api.RenameProjectCommand

data class RenameProjectDto(
    val identifier: ProjectId,
    val version: Long,
    val name: String,
) {
  fun toCommand() = RenameProjectCommand(identifier, version, name)
}
