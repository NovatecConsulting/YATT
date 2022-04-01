package com.novatecgmbh.eventsourcing.axon.user.user.grpc

import com.novatecgmbh.eventsourcing.axon.AllUsersQueryProto
import com.novatecgmbh.eventsourcing.axon.AllUsersQueryResultProto
import com.novatecgmbh.eventsourcing.axon.UserQueryResultProto
import com.novatecgmbh.eventsourcing.axon.UserServiceGrpc
import com.novatecgmbh.eventsourcing.axon.application.security.SecurityContextHelper
import com.novatecgmbh.eventsourcing.axon.user.api.AllUsersQuery
import com.novatecgmbh.eventsourcing.axon.user.api.UserQueryResult
import io.grpc.stub.StreamObserver
import net.devh.boot.grpc.server.service.GrpcService
import org.axonframework.extensions.kotlin.queryMany
import org.axonframework.queryhandling.QueryGateway

@GrpcService
class UserService(val queryGateway: QueryGateway) : UserServiceGrpc.UserServiceImplBase() {

  override fun findAll(
      request: AllUsersQueryProto,
      responseObserver: StreamObserver<AllUsersQueryResultProto>
  ) {
    SecurityContextHelper.setAuthentication("ca509f45-a184-4b62-8769-777df02aeaa5")
    queryGateway.queryMany<UserQueryResult, AllUsersQuery>(AllUsersQuery()).thenApply {
      it.map { it.toUserQueryResultProto() }.apply {
        AllUsersQueryResultProto.newBuilder().addAllUsers(this).build().apply {
          responseObserver.onNext(this)
          responseObserver.onCompleted()
        }
      }
    }
  }

  private fun UserQueryResult.toUserQueryResultProto(): UserQueryResultProto =
      UserQueryResultProto.newBuilder()
          .also {
            it.identifier = identifier.toString()
            it.firstname = firstname
            it.lastname = lastname
          }
          .build()
}
