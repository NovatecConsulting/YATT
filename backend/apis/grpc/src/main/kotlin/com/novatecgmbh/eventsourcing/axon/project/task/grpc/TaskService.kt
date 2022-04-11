package com.novatecgmbh.eventsourcing.axon.project.task.grpc

import com.google.protobuf.Empty
import com.novatecgmbh.eventsourcing.axon.*
import com.novatecgmbh.eventsourcing.axon.project.project.api.ProjectId
import com.novatecgmbh.eventsourcing.axon.project.task.api.TaskId
import com.novatecgmbh.eventsourcing.axon.project.task.api.TaskQuery
import com.novatecgmbh.eventsourcing.axon.project.task.api.TaskQueryResult
import com.novatecgmbh.eventsourcing.axon.project.task.api.TasksByProjectQuery
import io.grpc.stub.StreamObserver
import net.devh.boot.grpc.server.service.GrpcService
import org.axonframework.extensions.kotlin.queryMany
import org.axonframework.messaging.responsetypes.ResponseTypes
import org.axonframework.queryhandling.QueryGateway
import reactor.core.publisher.Flux

@GrpcService
class TaskService(val queryGateway: QueryGateway) : TaskServiceGrpc.TaskServiceImplBase() {

  override fun createTask(
      request: CreateTaskRequest?,
      responseObserver: StreamObserver<TaskIdentifier>?
  ) {
    super.createTask(request, responseObserver)
  }

  override fun renameTask(request: RenameTaskRequest?, responseObserver: StreamObserver<Empty>?) {
    super.renameTask(request, responseObserver)
  }

  override fun rescheduleTask(
      request: RescheduleTaskRequest?,
      responseObserver: StreamObserver<Empty>?
  ) {
    super.rescheduleTask(request, responseObserver)
  }

  override fun startTask(request: TaskIdentifier?, responseObserver: StreamObserver<Empty>?) {
    super.startTask(request, responseObserver)
  }

  override fun completeTask(request: TaskIdentifier?, responseObserver: StreamObserver<Empty>?) {
    super.completeTask(request, responseObserver)
  }

  override fun getTasksByProject(
      request: ProjectIdentifier,
      responseObserver: StreamObserver<TaskList>
  ) {
    queryGateway.queryMany<TaskQueryResult, TasksByProjectQuery>(
            TasksByProjectQuery(ProjectId(request.value)))
        .thenApply {
          it.map { it.toTask() }.apply {
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
        .doOnNext { responseObserver.onNext(it.toTask()) }
        .doOnError { responseObserver.onError(IllegalStateException()) }
        .doOnComplete { responseObserver.onCompleted() }
        .subscribe()
  }

  override fun getTaskById(request: TaskIdentifier?, responseObserver: StreamObserver<Task>?) {
    super.getTaskById(request, responseObserver)
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
        .doOnNext { responseObserver.onNext(it.toTask()) }
        .doOnError { responseObserver.onError(IllegalStateException()) }
        .doOnComplete { responseObserver.onCompleted() }
        .subscribe()
  }

  private fun TaskQueryResult.toTask(): Task =
      Task.newBuilder()
          .also {
            it.identifier = identifier.toString()
            it.name = name
            if (description != null) it.description = description
          }
          .build()
}
