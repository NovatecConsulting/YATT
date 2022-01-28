package com.novatecgmbh.eventsourcing.axon.project.task.web.dto

import com.novatecgmbh.eventsourcing.axon.project.task.api.RescheduleTaskCommand
import com.novatecgmbh.eventsourcing.axon.project.task.api.TaskId
import java.time.LocalDate

data class RescheduleTaskDto(val startDate: LocalDate, val endDate: LocalDate) {
  fun toCommand(taskId: TaskId) =
      RescheduleTaskCommand(identifier = taskId, startDate = startDate, endDate = endDate)
}
