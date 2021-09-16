package com.novatecgmbh.eventsourcing.axon.project.task.web.dto

import CreateTaskCommand
import com.novatecgmbh.eventsourcing.axon.project.project.command.ProjectId
import com.novatecgmbh.eventsourcing.axon.project.task.command.TaskId
import java.time.LocalDate

data class CreateTaskDto(
    val projectId: ProjectId,
    val name: String,
    val description: String?,
    val startDate: LocalDate,
    val endDate: LocalDate,
) {

  fun toCommand(identifier: TaskId? = null) =
      CreateTaskCommand(
          identifier = identifier ?: TaskId(),
          projectId = projectId,
          name = name,
          description = description,
          startDate = startDate,
          endDate = endDate)
}
