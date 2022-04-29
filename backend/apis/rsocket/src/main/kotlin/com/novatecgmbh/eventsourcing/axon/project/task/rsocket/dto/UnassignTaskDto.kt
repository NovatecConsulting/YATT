package com.novatecgmbh.eventsourcing.axon.project.task.rsocket.dto

import com.novatecgmbh.eventsourcing.axon.project.task.api.TaskId
import com.novatecgmbh.eventsourcing.axon.project.task.api.UnassignTaskCommand

data class UnassignTaskDto(val taskId: TaskId) {
  fun toCommand() = UnassignTaskCommand(identifier = taskId)
}
