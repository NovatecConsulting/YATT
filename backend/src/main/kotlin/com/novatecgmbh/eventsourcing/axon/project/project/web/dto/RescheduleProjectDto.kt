package com.novatecgmbh.eventsourcing.axon.project.project.web.dto

import com.novatecgmbh.eventsourcing.axon.project.project.api.ProjectId
import com.novatecgmbh.eventsourcing.axon.project.project.api.RescheduleProjectCommand
import java.time.LocalDate

data class RescheduleProjectDto(
    val aggregateVersion: Long,
    val newStartDate: LocalDate,
    val newDeadline: LocalDate
) {
  fun toCommand(projectId: ProjectId) =
      RescheduleProjectCommand(projectId, aggregateVersion, newStartDate, newDeadline)
}
