package com.novatecgmbh.grpc.client.demo.commands

import com.google.protobuf.Empty
import com.google.protobuf.util.JsonFormat
import com.novatecgmbh.eventsourcing.axon.UserServiceGrpc
import io.grpc.CallCredentials
import net.devh.boot.grpc.client.inject.GrpcClient
import org.springframework.shell.standard.ShellComponent
import org.springframework.shell.standard.ShellMethod

@ShellComponent
class UserCommands(private val callCredentials: CallCredentials) {

  @GrpcClient("grpcapi") private lateinit var userService: UserServiceGrpc.UserServiceBlockingStub

  @ShellMethod("List all registered users")
  fun users(): String {
    val users = userService.withCallCredentials(callCredentials).findAll(Empty.newBuilder().build())
    return JsonFormat.printer().print(users)
  }
}
