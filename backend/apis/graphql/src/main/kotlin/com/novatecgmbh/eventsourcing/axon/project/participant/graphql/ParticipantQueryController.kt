package com.novatecgmbh.eventsourcing.axon.project.participant.graphql

import com.novatecgmbh.eventsourcing.axon.project.participant.api.ParticipantByMultipleProjectsQuery
import com.novatecgmbh.eventsourcing.axon.project.participant.api.ParticipantId
import com.novatecgmbh.eventsourcing.axon.project.participant.api.ParticipantQuery
import com.novatecgmbh.eventsourcing.axon.project.participant.api.ParticipantQueryResult
import com.novatecgmbh.eventsourcing.axon.project.project.api.ProjectQueryResult
import com.novatecgmbh.eventsourcing.axon.project.task.api.TaskQueryResult
import java.util.concurrent.CompletableFuture
import org.axonframework.extensions.kotlin.query
import org.axonframework.extensions.kotlin.queryMany
import org.axonframework.queryhandling.QueryGateway
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.BatchMapping
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.graphql.data.method.annotation.SchemaMapping
import org.springframework.stereotype.Controller
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

@Controller
class ParticipantQueryController(val queryGateway: QueryGateway) {

  @QueryMapping
  fun participant(@Argument identifier: ParticipantId): CompletableFuture<ParticipantQueryResult> =
      queryGateway.query(ParticipantQuery(identifier))

  @BatchMapping(typeName = "Project")
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

  // TODO: Change to batch mapping
  @SchemaMapping(typeName = "Task")
  fun participant(task: TaskQueryResult): CompletableFuture<ParticipantQueryResult?> =
      if (task.participantId != null) {
        queryGateway.query(ParticipantQuery(task.participantId!!))
      } else CompletableFuture.completedFuture(null)
}
