package com.novatecgmbh.eventsourcing.axon.project.task.api

import com.novatecgmbh.eventsourcing.axon.project.participant.api.ParticipantId
import com.novatecgmbh.eventsourcing.axon.project.project.api.ProjectId
import java.time.LocalDate

data class TasksByProjectQuery(val projectId: ProjectId)

data class TasksByMultipleProjectsQuery(
    val projectIds: Set<ProjectId>,
    val from: LocalDate? = null,
    val to: LocalDate? = null
)

data class TaskQuery(val taskId: TaskId)

data class TaskQueryResult(
    val identifier: TaskId,
    val projectId: ProjectId,
    val name: String,
    val description: String?,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val status: TaskStatusEnum,
    val participantId: ParticipantId?,
    val assigneeFirstName: String?,
    val assigneeLastName: String?,
    val assigneeCompanyName: String?,
    val todos: List<TodoQueryResult>,
)

data class TodoQueryResult(val todoId: TodoId, val description: String, var isDone: Boolean)
