package com.novatecgmbh.eventsourcing.axon.project.task.rsocket

import com.novatecgmbh.eventsourcing.axon.project.project.api.ProjectId
import com.novatecgmbh.eventsourcing.axon.project.task.api.CreateTaskCommand
import com.novatecgmbh.eventsourcing.axon.project.task.api.TaskId
import java.time.LocalDate

data class CreateTaskDto(
    val identifier: TaskId = TaskId(),
    val projectId: ProjectId,
    val name: String,
    val description: String?,
    val startDate: LocalDate,
    val endDate: LocalDate
) {
  fun toCommand() =
      CreateTaskCommand(
          identifier = identifier,
          projectId = projectId,
          name = name,
          description = description,
          startDate = startDate,
          endDate = endDate)
}
