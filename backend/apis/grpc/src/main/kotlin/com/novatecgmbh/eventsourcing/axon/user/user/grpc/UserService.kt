package com.novatecgmbh.eventsourcing.axon.user.user.grpc

import com.google.protobuf.Empty
import com.novatecgmbh.eventsourcing.axon.User
import com.novatecgmbh.eventsourcing.axon.UserList
import com.novatecgmbh.eventsourcing.axon.UserServiceGrpc
import com.novatecgmbh.eventsourcing.axon.user.api.AllUsersQuery
import com.novatecgmbh.eventsourcing.axon.user.api.UserQueryResult
import io.grpc.stub.StreamObserver
import net.devh.boot.grpc.server.service.GrpcService
import org.axonframework.extensions.kotlin.queryMany
import org.axonframework.queryhandling.QueryGateway

@GrpcService
class UserService(val queryGateway: QueryGateway) : UserServiceGrpc.UserServiceImplBase() {

  override fun findAll(request: Empty, responseObserver: StreamObserver<UserList>) {
    queryGateway.queryMany<UserQueryResult, AllUsersQuery>(AllUsersQuery()).thenApply {
      it.map { it.toUserQueryResultProto() }.apply {
        UserList.newBuilder().addAllUsers(this).build().apply {
          responseObserver.onNext(this)
          responseObserver.onCompleted()
        }
      }
    }
  }

  private fun UserQueryResult.toUserQueryResultProto(): User =
      User.newBuilder()
          .also {
            it.identifier = identifier.toString()
            it.firstname = firstname
            it.lastname = lastname
          }
          .build()
}
