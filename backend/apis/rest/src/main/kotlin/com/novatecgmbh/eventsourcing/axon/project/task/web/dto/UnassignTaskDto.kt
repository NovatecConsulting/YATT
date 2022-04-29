package com.novatecgmbh.eventsourcing.axon.project.task.web.dto

import com.novatecgmbh.eventsourcing.axon.project.task.api.TaskId
import com.novatecgmbh.eventsourcing.axon.project.task.api.UnassignTaskCommand

class UnassignTaskDto {
  fun toCommand(taskId: TaskId) = UnassignTaskCommand(identifier = taskId)
}
