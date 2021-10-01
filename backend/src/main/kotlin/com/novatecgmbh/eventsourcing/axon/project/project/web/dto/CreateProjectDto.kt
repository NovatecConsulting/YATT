package com.novatecgmbh.eventsourcing.axon.project.project.web.dto

import com.novatecgmbh.eventsourcing.axon.project.project.api.CreateProjectCommand
import com.novatecgmbh.eventsourcing.axon.project.project.api.ProjectId
import java.time.LocalDate

data class CreateProjectDto(
    val name: String,
    val plannedStartDate: LocalDate,
    val deadline: LocalDate
) {
  fun toCommand(projectId: ProjectId) =
      CreateProjectCommand(projectId, name, plannedStartDate, deadline)
}
