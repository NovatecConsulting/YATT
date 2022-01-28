package com.novatecgmbh.eventsourcing.axon.project.project.web.dto

import com.novatecgmbh.eventsourcing.axon.project.project.api.ProjectId
import com.novatecgmbh.eventsourcing.axon.project.project.api.UpdateProjectCommand
import java.time.LocalDate

data class UpdateProjectDto(
    val version: Long,
    val name: String,
    val startDate: LocalDate,
    val deadline: LocalDate
) {
  fun toCommand(projectId: ProjectId) =
      UpdateProjectCommand(projectId, version, name, startDate, deadline)
}
