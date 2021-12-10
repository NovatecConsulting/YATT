package com.novatecgmbh.eventsourcing.axon.controller

import com.novatecgmbh.eventsourcing.axon.project.project.api.ProjectId
import com.novatecgmbh.eventsourcing.axon.project.project.api.ProjectQueryResult
import com.novatecgmbh.eventsourcing.axon.project.task.api.*
import java.time.LocalDate
import java.util.concurrent.CompletableFuture
import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.extensions.kotlin.queryMany
import org.axonframework.queryhandling.QueryGateway
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.BatchMapping
import org.springframework.graphql.data.method.annotation.MutationMapping
import org.springframework.stereotype.Controller
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

@Controller
class TaskController(val commandGateway: CommandGateway, val queryGateway: QueryGateway) {

  @BatchMapping
  fun tasks(
      projects: List<ProjectQueryResult>
  ): Mono<Map<ProjectQueryResult, List<TaskQueryResult>>> =
      queryGateway
          .queryMany<TaskQueryResult, TasksByMultipleProjectsQuery>(
              TasksByMultipleProjectsQuery(projects.map(ProjectQueryResult::identifier).toSet()))
          .thenApply {
            it.groupBy { task ->
              projects.first { project -> project.identifier == task.projectId }
            }
          }
          .toMono()

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
