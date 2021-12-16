package com.novatecgmbh.eventsourcing.axon.project.rsocket

import com.novatecgmbh.eventsourcing.axon.application.config.RegisteredUserProfile
import com.novatecgmbh.eventsourcing.axon.project.project.api.MyProjectsQuery
import com.novatecgmbh.eventsourcing.axon.project.project.api.ProjectQueryResult
import org.axonframework.extensions.reactor.queryhandling.gateway.ReactorQueryGateway
import org.axonframework.messaging.responsetypes.ResponseTypes
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
}
