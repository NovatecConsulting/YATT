package com.novatecgmbh.eventsourcing.axon.project.task.rsocket.dto

import com.novatecgmbh.eventsourcing.axon.project.task.api.RenameTaskCommand
import com.novatecgmbh.eventsourcing.axon.project.task.api.TaskId

data class RenameTaskDto(val identifier: TaskId, val name: String) {
  fun toCommand() = RenameTaskCommand(identifier = identifier, name = name)
}
