package com.novatecgmbh.eventsourcing.axon.project.project.rsocket

import com.novatecgmbh.eventsourcing.axon.application.security.RegisteredUserProfile
import com.novatecgmbh.eventsourcing.axon.project.project.api.*
import org.axonframework.extensions.reactor.queryhandling.gateway.ReactorQueryGateway
import org.axonframework.messaging.responsetypes.ResponseTypes
import org.springframework.messaging.handler.annotation.DestinationVariable
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.stereotype.Controller
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Controller
class ProjectController(val queryGateway: ReactorQueryGateway) {

  @MessageMapping("projects")
  fun getMyProjects(
      @AuthenticationPrincipal user: RegisteredUserProfile
  ): Mono<List<ProjectQueryResult>> =
      queryGateway.query(
          MyProjectsQuery(user.identifier),
          ResponseTypes.multipleInstancesOf(ProjectQueryResult::class.java))

  @MessageMapping("projects")
  fun subscribeMyProjectUpdates(
      @AuthenticationPrincipal user: RegisteredUserProfile
  ): Flux<ProjectQueryResult> =
      queryGateway.queryUpdates(MyProjectsQuery(user.identifier), ProjectQueryResult::class.java)

  @MessageMapping("projects.{id}")
  fun subscribeProjectByIdUpdates(@DestinationVariable id: ProjectId): Flux<ProjectQueryResult> =
      queryGateway.queryUpdates(ProjectQuery(id), ProjectQueryResult::class.java)

  @MessageMapping("projects.{id}.details")
  fun subscribeProjectDetailsByIdUpdates(
      @DestinationVariable id: ProjectId
  ): Flux<ProjectDetailsQueryResult> =
      queryGateway.queryUpdates(ProjectDetailsQuery(id), ProjectDetailsQueryResult::class.java)
}
