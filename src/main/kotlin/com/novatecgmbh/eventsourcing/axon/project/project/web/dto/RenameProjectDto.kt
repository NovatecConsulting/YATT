package com.novatecgmbh.eventsourcing.axon.project.project.web.dto

import com.novatecgmbh.eventsourcing.axon.project.project.api.ProjectId
import com.novatecgmbh.eventsourcing.axon.project.project.api.RenameProjectCommand

data class RenameProjectDto(val aggregateVersion: Long, val name: String) {
  fun toCommand(projectId: ProjectId) = RenameProjectCommand(projectId, aggregateVersion, name)
}
