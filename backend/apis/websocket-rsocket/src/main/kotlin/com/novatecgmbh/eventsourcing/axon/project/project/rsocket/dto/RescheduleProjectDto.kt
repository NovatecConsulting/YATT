package com.novatecgmbh.eventsourcing.axon.project.project.rsocket.dto

import com.novatecgmbh.eventsourcing.axon.project.project.api.ProjectId
import com.novatecgmbh.eventsourcing.axon.project.project.api.RescheduleProjectCommand
import java.time.LocalDate

data class RescheduleProjectDto(
    val identifier: ProjectId,
    val version: Long,
    val startDate: LocalDate,
    val deadline: LocalDate,
) {
  fun toCommand() = RescheduleProjectCommand(identifier, version, startDate, deadline)
}
