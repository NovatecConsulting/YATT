package com.novatecgmbh.eventsourcing.axon.project.project.web.dto

import com.novatecgmbh.eventsourcing.axon.project.project.api.ProjectId
import com.novatecgmbh.eventsourcing.axon.project.project.api.RescheduleProjectCommand
import java.time.LocalDate

data class RescheduleProjectDto(
    val version: Long,
    val startDate: LocalDate,
    val deadline: LocalDate
) {
  fun toCommand(projectId: ProjectId) =
      RescheduleProjectCommand(projectId, version, startDate, deadline)
}
