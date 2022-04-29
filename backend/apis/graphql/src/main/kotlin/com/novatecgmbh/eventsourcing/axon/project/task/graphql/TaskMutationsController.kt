package com.novatecgmbh.eventsourcing.axon.project.task.graphql

import com.novatecgmbh.eventsourcing.axon.project.participant.api.ParticipantId
import com.novatecgmbh.eventsourcing.axon.project.project.api.ProjectId
import com.novatecgmbh.eventsourcing.axon.project.task.api.*
import java.time.LocalDate
import java.util.concurrent.CompletableFuture
import org.axonframework.commandhandling.gateway.CommandGateway
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.MutationMapping
import org.springframework.stereotype.Controller

@Controller
class TaskMutationsController(val commandGateway: CommandGateway) {

  @MutationMapping
  fun createTask(
      @Argument projectIdentifier: ProjectId,
      @Argument name: String,
      @Argument description: String?,
      @Argument startDate: LocalDate,
      @Argument endDate: LocalDate
  ): CompletableFuture<TaskId> =
      commandGateway.send(
          CreateTaskCommand(TaskId(), projectIdentifier, name, description, startDate, endDate))

  @MutationMapping
  fun renameTask(@Argument identifier: TaskId, @Argument name: String): CompletableFuture<Long> =
      commandGateway.send(RenameTaskCommand(identifier, name))

  @MutationMapping
  fun rescheduleTask(
      @Argument identifier: TaskId,
      @Argument startDate: LocalDate,
      @Argument endDate: LocalDate
  ): CompletableFuture<Long> =
      commandGateway.send(RescheduleTaskCommand(identifier, startDate, endDate))

  @MutationMapping
  fun assignTask(
      @Argument identifier: TaskId,
      @Argument assignee: ParticipantId
  ): CompletableFuture<Long> = commandGateway.send(AssignTaskCommand(identifier, assignee))

  @MutationMapping
  fun unassignTask(@Argument identifier: TaskId): CompletableFuture<Long> =
      commandGateway.send(UnassignTaskCommand(identifier))

  @MutationMapping
  fun changeDescriptionOfTask(
      @Argument identifier: TaskId,
      @Argument description: String
  ): CompletableFuture<Long> =
      commandGateway.send(ChangeTaskDescriptionCommand(identifier, description))

  @MutationMapping
  fun startTask(@Argument identifier: TaskId): CompletableFuture<Long> =
      commandGateway.send(StartTaskCommand(identifier))

  @MutationMapping
  fun completeTask(@Argument identifier: TaskId): CompletableFuture<Long> =
      commandGateway.send(CompleteTaskCommand(identifier))

  @MutationMapping
  fun addTodoToTask(
      @Argument identifier: TaskId,
      @Argument description: String
  ): CompletableFuture<TodoId> =
      commandGateway.send(AddTodoCommand(identifier, TodoId(), description))

  @MutationMapping
  fun markTodoAsDone(
      @Argument identifier: TaskId,
      @Argument todoIdentifier: TodoId
  ): CompletableFuture<Long> =
      commandGateway.send(MarkTodoAsDoneCommand(identifier, todoIdentifier))

  @MutationMapping
  fun removeTodoFromTask(
      @Argument identifier: TaskId,
      @Argument todoIdentifier: TodoId
  ): CompletableFuture<Long> = commandGateway.send(RemoveTodoCommand(identifier, todoIdentifier))
}
