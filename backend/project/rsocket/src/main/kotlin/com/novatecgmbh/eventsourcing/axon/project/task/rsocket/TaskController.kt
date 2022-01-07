package com.novatecgmbh.eventsourcing.axon.project.task.rsocket

import com.novatecgmbh.eventsourcing.axon.project.project.api.ProjectId
import com.novatecgmbh.eventsourcing.axon.project.task.api.*
import com.novatecgmbh.eventsourcing.axon.project.task.rsocket.dtos.*
import org.axonframework.extensions.reactor.commandhandling.gateway.ReactorCommandGateway
import org.axonframework.extensions.reactor.queryhandling.gateway.ReactorQueryGateway
import org.springframework.messaging.handler.annotation.DestinationVariable
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.stereotype.Controller
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Controller
class TaskController(
    val queryGateway: ReactorQueryGateway,
    val commandGateway: ReactorCommandGateway
) {

  @MessageMapping("tasks.create")
  fun createTask(data: CreateTaskDto): Mono<TaskId> = commandGateway.send(data.toCommand())

  @MessageMapping("tasks.rename")
  fun renameTask(data: RenameTaskDto): Mono<Unit> = commandGateway.send(data.toCommand())

  @MessageMapping("tasks.reschedule")
  fun rescheduleTask(data: RescheduleTaskDto): Mono<Unit> = commandGateway.send(data.toCommand())

  @MessageMapping("tasks.{id}.start")
  fun startTask(@DestinationVariable id: TaskId): Mono<Unit> =
      commandGateway.send(StartTaskCommand(id))

  @MessageMapping("tasks.{id}.complete")
  fun completeTask(@DestinationVariable id: TaskId): Mono<Unit> =
      commandGateway.send(CompleteTaskCommand(id))

  @MessageMapping("tasks.todos.add")
  fun addTodo(data: AddTodoDto): Mono<Unit> = commandGateway.send(data.toCommand())

  @MessageMapping("tasks.todos.remove")
  fun removeTodo(data: RemoveTodoDto): Mono<Unit> = commandGateway.send(data.toCommand())

  @MessageMapping("tasks.todos.markDone")
  fun markTodoAsDone(data: MarkTodoAsDoneDto): Mono<Unit> = commandGateway.send(data.toCommand())

  @MessageMapping("tasks.{id}")
  fun subscribeTaskByIdUpdates(@DestinationVariable id: TaskId): Flux<TaskQueryResult> =
      queryGateway.queryUpdates(TaskQuery(id), TaskQueryResult::class.java)

  @MessageMapping("projects.{id}.tasks")
  fun subscribeTaskByProjectUpdates(@DestinationVariable id: ProjectId): Flux<TaskQueryResult> =
      queryGateway.queryUpdates(TasksByProjectQuery(id), TaskQueryResult::class.java)
}
