package com.novatecgmbh.eventsourcing.axon.project.task.web.dto

import com.novatecgmbh.eventsourcing.axon.project.project.api.ProjectId
import com.novatecgmbh.eventsourcing.axon.project.task.api.CreateTaskCommand
import com.novatecgmbh.eventsourcing.axon.project.task.api.TaskId
import java.time.LocalDate

data class CreateTaskDto(
    val projectId: ProjectId,
    val name: String,
    val description: String?,
    val startDate: LocalDate,
    val endDate: LocalDate
) {
  fun toCommand(identifier: TaskId) =
      CreateTaskCommand(
          identifier = identifier,
          projectId = projectId,
          name = name,
          description = description,
          startDate = startDate,
          endDate = endDate)
}
