package com.novatecgmbh.eventsourcing.axon.project.task.rsocket.dto

import com.novatecgmbh.eventsourcing.axon.project.participant.api.ParticipantId
import com.novatecgmbh.eventsourcing.axon.project.task.api.AssignTaskCommand
import com.novatecgmbh.eventsourcing.axon.project.task.api.TaskId

data class AssignTaskDto(val taskId: TaskId, val assignee: ParticipantId) {
  fun toCommand() = AssignTaskCommand(identifier = taskId, assignee = assignee)
}
