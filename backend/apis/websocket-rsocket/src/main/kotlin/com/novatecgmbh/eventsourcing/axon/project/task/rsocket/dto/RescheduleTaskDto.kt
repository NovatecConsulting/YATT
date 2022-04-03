package com.novatecgmbh.eventsourcing.axon.project.task.rsocket.dto

import com.novatecgmbh.eventsourcing.axon.project.task.api.RescheduleTaskCommand
import com.novatecgmbh.eventsourcing.axon.project.task.api.TaskId
import java.time.LocalDate

data class RescheduleTaskDto(val identifier: TaskId, val startDate: LocalDate, val endDate: LocalDate) {
  fun toCommand() =
      RescheduleTaskCommand(identifier = identifier, startDate = startDate, endDate = endDate)
}
