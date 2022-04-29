package com.novatecgmbh.eventsourcing.axon.project.task.web.dto

import com.novatecgmbh.eventsourcing.axon.project.participant.api.ParticipantId
import com.novatecgmbh.eventsourcing.axon.project.task.api.AssignTaskCommand
import com.novatecgmbh.eventsourcing.axon.project.task.api.TaskId

data class AssignTaskDto(val assignee: ParticipantId) {
  fun toCommand(taskId: TaskId) = AssignTaskCommand(identifier = taskId, assignee = assignee)
}
