package com.novatecgmbh.grpc.client.demo.commands

import com.google.protobuf.util.JsonFormat
import com.novatecgmbh.eventsourcing.axon.ProjectIdentifier
import com.novatecgmbh.eventsourcing.axon.Task
import com.novatecgmbh.eventsourcing.axon.TaskIdentifier
import com.novatecgmbh.eventsourcing.axon.TaskServiceGrpc
import io.grpc.CallCredentials
import io.grpc.stub.ClientCallStreamObserver
import io.grpc.stub.ClientResponseObserver
import io.grpc.stub.StreamObserver
import net.devh.boot.grpc.client.inject.GrpcClient
import org.slf4j.LoggerFactory
import org.springframework.shell.standard.ShellCommandGroup
import org.springframework.shell.standard.ShellComponent
import org.springframework.shell.standard.ShellMethod

@ShellComponent
@ShellCommandGroup("tasks")
class TaskCommands(private val callCredentials: CallCredentials) {

  private val LOGGER = LoggerFactory.getLogger(TaskCommands::class.java)

  @GrpcClient("grpcapi") private lateinit var taskServiceBlocking: TaskServiceGrpc.TaskServiceBlockingStub

  @GrpcClient("grpcapi") private lateinit var taskService: TaskServiceGrpc.TaskServiceStub

  private val subscriptions: MutableMap<ProjectIdentifier, ClientCallStreamObserver<ProjectIdentifier>> = mutableMapOf()

  @ShellMethod("Subscribe to tasks of given project identifier")
  fun subscribe(identifier: String) {
      ProjectIdentifier.newBuilder().setValue(identifier).build().let {
          val observer: StreamObserver<Task> = object: StreamObserver<Task>, ClientResponseObserver<ProjectIdentifier, Task> {

              override fun onNext(value: Task) {
                  LOGGER.info(JsonFormat.printer().print(value))
              }

              override fun onError(t: Throwable) {
                  LOGGER.info(t.message)
              }

              override fun onCompleted() {
                  LOGGER.info("Subscription cancelled.")
              }

              override fun beforeStart(requestStream: ClientCallStreamObserver<ProjectIdentifier>) {
                  subscriptions[ProjectIdentifier.newBuilder().setValue(identifier).build()] = requestStream
              }
          }
          taskService.subscribeTasksByProject(it, observer)
      }
  }

  @ShellMethod("Cancel subscription to tasks of given project identifier")
  fun cancel(identifier: String): String {
      ProjectIdentifier.newBuilder().setValue(identifier).build().apply {
          return if (subscriptions.containsKey(this)) {
              subscriptions[this]!!.cancel("Subscription cancelled by client", null)
              subscriptions.remove(this)
              "Subscription cancelled."
          } else {
              "No subscription for this project identifier found."
          }
      }

  }

  @ShellMethod("List all tasks for given project identifier")
  fun list(identifier: String) =
      ProjectIdentifier.newBuilder()
          .setValue(identifier)
          .build()
          .let { taskServiceBlocking.withCallCredentials(callCredentials).getTasksByProject(it) }
          .let { JsonFormat.printer().print(it) }

  @ShellMethod("Start task with given identifier")
  fun start(identifier: String): String {
    taskServiceBlocking
        .withCallCredentials(callCredentials)
        .startTask(TaskIdentifier.newBuilder().setValue(identifier).build())
    return "Task started"
  }
}
