package com.novatecgmbh.eventsourcing.axon.project.task.api

import com.novatecgmbh.eventsourcing.axon.project.participant.api.ParticipantId
import com.novatecgmbh.eventsourcing.axon.project.project.api.ProjectId
import java.time.LocalDate
import org.axonframework.modelling.command.TargetAggregateIdentifier

abstract class TaskCommand(
    open val identifier: TaskId,
)

data class CreateTaskCommand(
    @TargetAggregateIdentifier override val identifier: TaskId,
    val projectId: ProjectId,
    val name: String,
    val description: String?,
    val startDate: LocalDate,
    val endDate: LocalDate
) : TaskCommand(identifier)

data class RenameTaskCommand(
    @TargetAggregateIdentifier override val identifier: TaskId,
    val name: String,
) : TaskCommand(identifier)

data class ChangeTaskDescriptionCommand(
    @TargetAggregateIdentifier override val identifier: TaskId,
    val description: String
) : TaskCommand(identifier)

data class RescheduleTaskCommand(
    @TargetAggregateIdentifier override val identifier: TaskId,
    val startDate: LocalDate,
    val endDate: LocalDate
) : TaskCommand(identifier)

data class AssignTaskCommand(
    @TargetAggregateIdentifier override val identifier: TaskId,
    val assignee: ParticipantId
) : TaskCommand(identifier)

data class UnassignTaskCommand(@TargetAggregateIdentifier override val identifier: TaskId) :
    TaskCommand(identifier)

data class StartTaskCommand(@TargetAggregateIdentifier override val identifier: TaskId) :
    TaskCommand(identifier)

data class CompleteTaskCommand(@TargetAggregateIdentifier override val identifier: TaskId) :
    TaskCommand(identifier)

data class AddTodoCommand(
    @TargetAggregateIdentifier override val identifier: TaskId,
    val todoId: TodoId,
    val description: String
) : TaskCommand(identifier)

data class MarkTodoAsDoneCommand(
    @TargetAggregateIdentifier override val identifier: TaskId,
    val todoId: TodoId
) : TaskCommand(identifier)

data class RemoveTodoCommand(
    @TargetAggregateIdentifier override val identifier: TaskId,
    val todoId: TodoId
) : TaskCommand(identifier)
