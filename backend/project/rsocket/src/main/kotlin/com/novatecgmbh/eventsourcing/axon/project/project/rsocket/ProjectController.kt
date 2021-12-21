package com.novatecgmbh.eventsourcing.axon.project.project.rsocket

import com.novatecgmbh.eventsourcing.axon.application.security.RegisteredUserProfile
import com.novatecgmbh.eventsourcing.axon.project.project.api.*
import com.novatecgmbh.eventsourcing.axon.project.project.rsocket.dtos.*
import org.axonframework.extensions.reactor.commandhandling.gateway.ReactorCommandGateway
import org.axonframework.extensions.reactor.queryhandling.gateway.ReactorQueryGateway
import org.axonframework.messaging.responsetypes.ResponseTypes
import org.springframework.messaging.handler.annotation.DestinationVariable
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.stereotype.Controller
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Controller
class ProjectController(
    val queryGateway: ReactorQueryGateway,
    val commandGateway: ReactorCommandGateway
) {

  @MessageMapping("projects.create")
  fun createProject(data: CreateProjectDto): Mono<ProjectId> = commandGateway.send(data.toCommand())

  @MessageMapping("projects.rename")
  fun renameProject(data: RenameProjectDto): Mono<Unit> = commandGateway.send(data.toCommand())

  @MessageMapping("projects.reschedule")
  fun rescheduleProject(data: RescheduleProjectDto): Mono<Unit> =
      commandGateway.send(data.toCommand())

  @MessageMapping("projects.get")
  fun getMyProjects(
      @AuthenticationPrincipal user: RegisteredUserProfile
  ): Mono<List<ProjectQueryResult>> =
      queryGateway.query(
          MyProjectsQuery(user.identifier),
          ResponseTypes.multipleInstancesOf(ProjectQueryResult::class.java))

  @MessageMapping("projects.updates")
  fun subscribeMyProjectUpdates(
      @AuthenticationPrincipal user: RegisteredUserProfile
  ): Flux<ProjectQueryResult> =
      queryGateway.queryUpdates(MyProjectsQuery(user.identifier), ProjectQueryResult::class.java)

  @MessageMapping("projects.{id}.updates")
  fun subscribeProjectByIdUpdates(@DestinationVariable id: ProjectId): Flux<ProjectQueryResult> =
      queryGateway.queryUpdates(ProjectQuery(id), ProjectQueryResult::class.java)

  @MessageMapping("projects.{id}.details.updates")
  fun subscribeProjectDetailsByIdUpdates(
      @DestinationVariable id: ProjectId
  ): Flux<ProjectDetailsQueryResult> =
      queryGateway.queryUpdates(ProjectDetailsQuery(id), ProjectDetailsQueryResult::class.java)
}
