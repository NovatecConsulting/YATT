package com.novatecgmbh.eventsourcing.axon.project.project.grpc

import com.google.protobuf.Empty
import com.novatecgmbh.eventsourcing.axon.Project
import com.novatecgmbh.eventsourcing.axon.ProjectList
import com.novatecgmbh.eventsourcing.axon.ProjectServiceGrpc
import com.novatecgmbh.eventsourcing.axon.application.security.SecurityContextHelper
import com.novatecgmbh.eventsourcing.axon.project.project.api.MyProjectsQuery
import com.novatecgmbh.eventsourcing.axon.project.project.api.ProjectQueryResult
import com.novatecgmbh.eventsourcing.axon.project.toTimestamp
import io.grpc.stub.StreamObserver
import net.devh.boot.grpc.server.service.GrpcService
import org.axonframework.extensions.kotlin.queryMany
import org.axonframework.queryhandling.QueryGateway

@GrpcService
class ProjectService(val queryGateway: QueryGateway) : ProjectServiceGrpc.ProjectServiceImplBase() {

  override fun findMyProjects(request: Empty, responseObserver: StreamObserver<ProjectList>) {
    val user = SecurityContextHelper.getUser()!!
    queryGateway.queryMany<ProjectQueryResult, MyProjectsQuery>(MyProjectsQuery(user)).thenApply {
      it.map { it.toProjectProto() }.apply {
        ProjectList.newBuilder().addAllProjects(this).build().apply {
          responseObserver.onNext(this)
          responseObserver.onCompleted()
        }
      }
    }
  }

  private fun ProjectQueryResult.toProjectProto(): Project =
      Project.newBuilder()
          .also {
            it.identifier = identifier.toString()
            it.name = name
            it.startDate = startDate.toTimestamp()
            it.deadline = deadline.toTimestamp()
            if (actualEndDate != null) it.actualEndDate = actualEndDate!!.toTimestamp()
          }
          .build()
}
