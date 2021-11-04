package com.novatecgmbh.eventsourcing.axon.project.task.web.dto

import com.novatecgmbh.eventsourcing.axon.project.task.api.RenameTaskCommand
import com.novatecgmbh.eventsourcing.axon.project.task.api.TaskId

data class RenameTaskDto(val name: String) {
  fun toCommand(taskId: TaskId) = RenameTaskCommand(identifier = taskId, name = name)
}
