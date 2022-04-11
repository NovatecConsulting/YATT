package com.novatecgmbh.eventsourcing.axon.project.task.grpc

import com.google.protobuf.Empty
import com.novatecgmbh.eventsourcing.axon.*
import com.novatecgmbh.eventsourcing.axon.Task.TaskStatus
import com.novatecgmbh.eventsourcing.axon.project.project.api.ProjectId
import com.novatecgmbh.eventsourcing.axon.project.task.api.*
import com.novatecgmbh.eventsourcing.axon.project.toLocalDate
import com.novatecgmbh.eventsourcing.axon.project.toTimestamp
import io.grpc.stub.StreamObserver
import net.devh.boot.grpc.server.service.GrpcService
import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.extensions.kotlin.queryMany
import org.axonframework.extensions.kotlin.queryOptional
import org.axonframework.messaging.responsetypes.ResponseTypes
import org.axonframework.queryhandling.QueryGateway
import reactor.core.publisher.Flux

@GrpcService
class TaskService(val queryGateway: QueryGateway, val commandGateway: CommandGateway) :
    TaskServiceGrpc.TaskServiceImplBase() {

  override fun createTask(
      request: CreateTaskRequest,
      responseObserver: StreamObserver<TaskIdentifier>
  ) {
    commandGateway.send<TaskId>(request.toCommand()).whenComplete { result, throwable ->
      if (throwable != null) {
        responseObserver.onError(throwable)
      } else {
        responseObserver.onNext(TaskIdentifier.newBuilder().setValue(result.identifier).build())
        responseObserver.onCompleted()
      }
    }
  }

  override fun renameTask(request: RenameTaskRequest, responseObserver: StreamObserver<Empty>) {
    commandGateway.send<Long>(request.toCommand()).whenComplete { _, throwable ->
      if (throwable != null) {
        responseObserver.onError(throwable)
      } else {
        responseObserver.onNext(Empty.newBuilder().build())
        responseObserver.onCompleted()
      }
    }
  }

  override fun rescheduleTask(
      request: RescheduleTaskRequest,
      responseObserver: StreamObserver<Empty>
  ) {
    commandGateway.send<Long>(request.toCommand()).whenComplete { _, throwable ->
      if (throwable != null) {
        responseObserver.onError(throwable)
      } else {
        responseObserver.onNext(Empty.newBuilder().build())
        responseObserver.onCompleted()
      }
    }
  }

  override fun startTask(request: TaskIdentifier, responseObserver: StreamObserver<Empty>) {
    commandGateway.send<Long>(StartTaskCommand(TaskId(request.value))).whenComplete { _, throwable
      ->
      if (throwable != null) {
        responseObserver.onError(throwable)
      } else {
        responseObserver.onNext(Empty.newBuilder().build())
        responseObserver.onCompleted()
      }
    }
  }

  override fun completeTask(request: TaskIdentifier, responseObserver: StreamObserver<Empty>) {
    commandGateway.send<Long>(CompleteTaskCommand(TaskId(request.value))).whenComplete {
        _,
        throwable ->
      if (throwable != null) {
        responseObserver.onError(throwable)
      } else {
        responseObserver.onNext(Empty.newBuilder().build())
        responseObserver.onCompleted()
      }
    }
  }

  override fun getTasksByProject(
      request: ProjectIdentifier,
      responseObserver: StreamObserver<TaskList>
  ) {
    queryGateway.queryMany<TaskQueryResult, TasksByProjectQuery>(
            TasksByProjectQuery(ProjectId(request.value)))
        .thenApply {
          it.map { it.toProto() }.apply {
            TaskList.newBuilder().addAllTasks(this).build().apply {
              responseObserver.onNext(this)
              responseObserver.onCompleted()
            }
          }
        }
  }

  override fun subscribeTasksByProject(
      request: ProjectIdentifier,
      responseObserver: StreamObserver<Task>
  ) {
    val query =
        queryGateway.subscriptionQuery(
            TasksByProjectQuery(ProjectId(request.value)),
            ResponseTypes.multipleInstancesOf(TaskQueryResult::class.java),
            ResponseTypes.instanceOf(TaskQueryResult::class.java))
    val combinedResult =
        query.initialResult().flatMapMany { Flux.fromIterable(it) }.concatWith(query.updates())
    combinedResult
        .doOnNext { responseObserver.onNext(it.toProto()) }
        .doOnError { responseObserver.onError(IllegalStateException()) }
        .doOnComplete { responseObserver.onCompleted() }
        .doOnCancel { responseObserver.onCompleted() }
        .onErrorResume {
          query.cancel()
          Flux.just()
        }
        .subscribe()
  }

  override fun getTaskById(request: TaskIdentifier, responseObserver: StreamObserver<Task>) {
    queryGateway
        .queryOptional<TaskQueryResult, TaskQuery>(TaskQuery(TaskId(request.value)))
        .thenApply { it.map { responseObserver.onNext(it.toProto()) } }
        .thenApply { responseObserver.onCompleted() }
  }

  override fun subscribeTaskById(request: TaskIdentifier, responseObserver: StreamObserver<Task>) {
    val query =
        queryGateway.subscriptionQuery(
            TaskQuery(TaskId(request.value)),
            ResponseTypes.instanceOf(TaskQueryResult::class.java),
            ResponseTypes.instanceOf(TaskQueryResult::class.java))

    val combinedResult =
        query.initialResult().concatWith(query.updates()).doFinally { query.cancel() }

    combinedResult
        .doOnNext { responseObserver.onNext(it.toProto()) }
        .doOnError { responseObserver.onError(IllegalStateException()) }
        .doOnComplete { responseObserver.onCompleted() }
        .subscribe()
  }

  private fun TaskQueryResult.toProto(): Task =
      Task.newBuilder()
          .also {
            it.identifier = identifier.toString()
            it.name = name
            if (description != null) it.description = description
            it.status = TaskStatus.valueOf(status.name)
            it.startDate = startDate.toTimestamp()
            it.endDate = endDate.toTimestamp()
            it.addAllTodo(todos.map { todo -> todo.toProto() })
          }
          .build()

  private fun TodoQueryResult.toProto(): Todo =
      Todo.newBuilder()
          .also {
            it.identifier = todoId.identifier
            it.name = description
            it.done = isDone
          }
          .build()

  private fun CreateTaskRequest.toCommand(): CreateTaskCommand =
      CreateTaskCommand(
          identifier = TaskId(),
          projectId = ProjectId(this.projectIdentifier.value),
          name = this.name,
          description = this.description,
          startDate = this.startDate.toLocalDate(),
          endDate = this.endDate.toLocalDate())

  private fun RenameTaskRequest.toCommand(): RenameTaskCommand =
      RenameTaskCommand(identifier = TaskId(this.identifier.value), name = this.name)

  private fun RescheduleTaskRequest.toCommand(): RescheduleTaskCommand =
      RescheduleTaskCommand(
          identifier = TaskId(this.identifier.value),
          startDate = this.startDate.toLocalDate(),
          endDate = this.endDate.toLocalDate())
}
