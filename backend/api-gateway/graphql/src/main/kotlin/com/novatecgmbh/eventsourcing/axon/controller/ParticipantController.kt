package com.novatecgmbh.eventsourcing.axon.controller

import com.novatecgmbh.eventsourcing.axon.company.company.api.CompanyId
import com.novatecgmbh.eventsourcing.axon.project.participant.api.CreateParticipantCommand
import com.novatecgmbh.eventsourcing.axon.project.participant.api.ParticipantByMultipleProjectsQuery
import com.novatecgmbh.eventsourcing.axon.project.participant.api.ParticipantId
import com.novatecgmbh.eventsourcing.axon.project.participant.api.ParticipantQueryResult
import com.novatecgmbh.eventsourcing.axon.project.project.api.ProjectId
import com.novatecgmbh.eventsourcing.axon.project.project.api.ProjectQueryResult
import com.novatecgmbh.eventsourcing.axon.user.api.UserId
import java.util.concurrent.CompletableFuture
import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.extensions.kotlin.queryMany
import org.axonframework.queryhandling.QueryGateway
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.BatchMapping
import org.springframework.graphql.data.method.annotation.MutationMapping
import org.springframework.stereotype.Controller
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

@Controller
class ParticipantController(val commandGateway: CommandGateway, val queryGateway: QueryGateway) {

  @BatchMapping
  fun participants(
      projects: List<ProjectQueryResult>
  ): Mono<Map<ProjectQueryResult, List<ParticipantQueryResult>>> =
      queryGateway
          .queryMany<ParticipantQueryResult, ParticipantByMultipleProjectsQuery>(
              ParticipantByMultipleProjectsQuery(
                  projects.map((ProjectQueryResult::identifier)).toSet()))
          .thenApply {
            it.groupBy { participant ->
              projects.first { project -> project.identifier == participant.projectId }
            }
          }
          .toMono()

  @MutationMapping
  fun createParticipant(
      @Argument projectIdentifier: ProjectId,
      @Argument companyIdentifier: CompanyId,
      @Argument userIdentifier: UserId
  ): CompletableFuture<ParticipantId> =
      commandGateway.send(
          CreateParticipantCommand(
              ParticipantId(), projectIdentifier, companyIdentifier, userIdentifier))
}
