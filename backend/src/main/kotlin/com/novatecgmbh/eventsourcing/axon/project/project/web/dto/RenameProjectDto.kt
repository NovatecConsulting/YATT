package com.novatecgmbh.eventsourcing.axon.project.project.web.dto

import com.novatecgmbh.eventsourcing.axon.project.project.api.ProjectId
import com.novatecgmbh.eventsourcing.axon.project.project.api.RenameProjectCommand

data class RenameProjectDto(val version: Long, val name: String) {
  fun toCommand(projectId: ProjectId) = RenameProjectCommand(projectId, version, name)
}
