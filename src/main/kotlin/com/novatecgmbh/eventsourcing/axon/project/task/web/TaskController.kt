package com.novatecgmbh.eventsourcing.axon.project.task.web

import com.novatecgmbh.eventsourcing.axon.project.project.api.ProjectId
import com.novatecgmbh.eventsourcing.axon.project.task.api.*
import com.novatecgmbh.eventsourcing.axon.project.task.web.dto.CreateTaskDto
import com.novatecgmbh.eventsourcing.axon.project.task.web.dto.RescheduleTaskDto
import java.util.concurrent.CompletableFuture
import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.extensions.kotlin.queryMany
import org.axonframework.extensions.kotlin.queryOptional
import org.axonframework.messaging.responsetypes.ResponseTypes
import org.axonframework.queryhandling.QueryGateway
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.http.HttpStatus.OK
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux

@RestController
class TaskController(
    private val commandGateway: CommandGateway,
    private val queryGateway: QueryGateway,
) {

  @PostMapping("/v2/tasks")
  fun createTask(@RequestBody body: CreateTaskDto): CompletableFuture<String> =
      commandGateway.send(body.toCommand())

  @PostMapping("/v2/tasks/{taskId}")
  fun createTaskWithId(
      @PathVariable("taskId") taskId: TaskId,
      @RequestBody body: CreateTaskDto
  ): CompletableFuture<String> = commandGateway.send(body.toCommand(taskId))

  @GetMapping("/v2/tasks/{taskId}")
  fun getTaskById(@PathVariable("taskId") taskId: TaskId): ResponseEntity<TaskQueryResult> =
      queryGateway
          .queryOptional<TaskQueryResult, TaskQuery>(TaskQuery(taskId))
          .get()
          .map { ResponseEntity(it, OK) }
          .orElse(ResponseEntity(NOT_FOUND))

  @GetMapping("/v2/tasks/{taskId}", produces = [MediaType.APPLICATION_NDJSON_VALUE])
  fun getTaskByIdAndUpdates(@PathVariable("taskId") taskId: TaskId): Flux<TaskQueryResult> {
    val query =
        queryGateway.subscriptionQuery(
            TaskQuery(taskId),
            ResponseTypes.instanceOf(TaskQueryResult::class.java),
            ResponseTypes.instanceOf(TaskQueryResult::class.java))

    return query.initialResult().concatWith(query.updates())
  }

  @GetMapping("/v2/projects/{projectId}/tasks")
  fun getTasksByProject(
      @PathVariable("projectId") projectId: ProjectId
  ): ResponseEntity<List<TaskQueryResult>> =
      ResponseEntity(
          queryGateway
              .queryMany<TaskQueryResult, TasksByProjectQuery>(TasksByProjectQuery(projectId))
              .get(),
          OK)

  @GetMapping("/v2/projects/{projectId}/tasks", produces = [MediaType.APPLICATION_NDJSON_VALUE])
  fun getTasksByProjectAndUpdates(
      @PathVariable("projectId") projectId: ProjectId
  ): Flux<TaskQueryResult> {
    val query =
        queryGateway.subscriptionQuery(
            TasksByProjectQuery(projectId),
            ResponseTypes.multipleInstancesOf(TaskQueryResult::class.java),
            ResponseTypes.instanceOf(TaskQueryResult::class.java))

    return query.initialResult().flatMapMany { Flux.fromIterable(it) }.concatWith(query.updates())
  }

  @PostMapping("/v2/tasks/{taskId}/reschedule")
  fun reschedule(
      @PathVariable("taskId") taskId: TaskId,
      @RequestBody body: RescheduleTaskDto
  ): CompletableFuture<String> = commandGateway.send(body.toCommand(taskId))

  @PostMapping("/v2/tasks/{taskId}/start")
  fun start(@PathVariable("taskId") taskId: TaskId): CompletableFuture<String> =
      commandGateway.send(StartTaskCommand(taskId))

  @PostMapping("/v2/tasks/{taskId}/complete")
  fun complete(@PathVariable("taskId") taskId: TaskId): CompletableFuture<String> =
      commandGateway.send(CompleteTaskCommand(taskId))
}
