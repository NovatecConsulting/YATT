package com.novatecgmbh.eventsourcing.axon.project.task.api

import com.novatecgmbh.eventsourcing.axon.project.participant.api.ParticipantId
import com.novatecgmbh.eventsourcing.axon.project.project.api.ProjectId
import java.time.LocalDate

abstract class TaskEvent(open val identifier: TaskId)

data class TaskCreatedEvent(
    override val identifier: TaskId,
    val projectId: ProjectId,
    val name: String,
    val description: String?,
    val startDate: LocalDate,
    val endDate: LocalDate
) : TaskEvent(identifier)

data class TaskRenamedEvent(override val identifier: TaskId, val name: String) :
    TaskEvent(identifier)

data class TaskDescriptionUpdatedEvent(override val identifier: TaskId, val description: String) :
    TaskEvent(identifier)

data class TaskRescheduledEvent(
    override val identifier: TaskId,
    val startDate: LocalDate,
    val endDate: LocalDate
) : TaskEvent(identifier)

data class TaskAssignedEvent(override val identifier: TaskId, val assignee: ParticipantId) :
    TaskEvent(identifier)

data class TaskUnassignedEvent(override val identifier: TaskId) : TaskEvent(identifier)

data class TaskStartedEvent(override val identifier: TaskId) : TaskEvent(identifier)

data class TaskCompletedEvent(override val identifier: TaskId) : TaskEvent(identifier)

data class TodoAddedEvent(
    override val identifier: TaskId,
    val todoId: TodoId,
    val description: String,
    val isDone: Boolean,
) : TaskEvent(identifier)

data class TodoMarkedAsDoneEvent(override val identifier: TaskId, val todoId: TodoId) :
    TaskEvent(identifier)

data class TodoRemovedEvent(override val identifier: TaskId, val todoId: TodoId) :
    TaskEvent(identifier)
