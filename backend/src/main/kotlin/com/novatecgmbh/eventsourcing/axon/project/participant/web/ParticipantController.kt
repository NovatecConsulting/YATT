package com.novatecgmbh.eventsourcing.axon.project.participant.web

import com.novatecgmbh.eventsourcing.axon.project.participant.api.ParticipantByProjectQuery
import com.novatecgmbh.eventsourcing.axon.project.participant.api.ParticipantId
import com.novatecgmbh.eventsourcing.axon.project.participant.api.ParticipantQuery
import com.novatecgmbh.eventsourcing.axon.project.participant.api.ParticipantQueryResult
import com.novatecgmbh.eventsourcing.axon.project.participant.web.dto.CreateParticipantDto
import com.novatecgmbh.eventsourcing.axon.project.project.api.ProjectId
import java.util.concurrent.CompletableFuture
import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.extensions.kotlin.queryMany
import org.axonframework.extensions.kotlin.queryOptional
import org.axonframework.messaging.responsetypes.ResponseTypes
import org.axonframework.queryhandling.QueryGateway
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux

@RestController
class ParticipantController(
    private val commandGateway: CommandGateway,
    private val queryGateway: QueryGateway,
) {
  @PostMapping("/v2/participants")
  fun createParticipant(@RequestBody body: CreateParticipantDto): CompletableFuture<String> =
      createParticipantWithId(ParticipantId(), body)

  @PostMapping("/v2/participants/{participantId}")
  fun createParticipantWithId(
      @PathVariable("participantId") participantId: ParticipantId,
      @RequestBody body: CreateParticipantDto,
  ): CompletableFuture<String> = commandGateway.send(body.toCommand(participantId))

  @GetMapping("/v2/participants/{participantId}")
  fun getParticipantById(
      @PathVariable("participantId") participantId: ParticipantId
  ): ResponseEntity<ParticipantQueryResult> =
      queryGateway
          .queryOptional<ParticipantQueryResult, ParticipantQuery>(ParticipantQuery(participantId))
          .get()
          .map { ResponseEntity(it, HttpStatus.OK) }
          .orElse(ResponseEntity(HttpStatus.NOT_FOUND))

  @GetMapping("/v2/participants/{participantId}", produces = [MediaType.APPLICATION_NDJSON_VALUE])
  fun getParticipantByIdAndUpdates(
      @PathVariable("participantId") participantId: ParticipantId
  ): Flux<ParticipantQueryResult> {
    val query =
        queryGateway.subscriptionQuery(
            ParticipantQuery(participantId),
            ResponseTypes.instanceOf(ParticipantQueryResult::class.java),
            ResponseTypes.instanceOf(ParticipantQueryResult::class.java))

    return query.initialResult().concatWith(query.updates()).doFinally { query.cancel() }
  }

  @GetMapping("/v2/projects/{projectId}/participants")
  fun getParticipantsByProject(
      @PathVariable("projectId") projectId: ProjectId
  ): ResponseEntity<List<ParticipantQueryResult>> =
      ResponseEntity(
          queryGateway
              .queryMany<ParticipantQueryResult, ParticipantByProjectQuery>(
                  ParticipantByProjectQuery(projectId))
              .get(),
          HttpStatus.OK)

  @GetMapping(
      "/v2/projects/{projectId}/participants", produces = [MediaType.APPLICATION_NDJSON_VALUE])
  fun getParticipantsByProjectAndUpdates(
      @PathVariable("projectId") projectId: ProjectId
  ): Flux<ParticipantQueryResult> {
    val query =
        queryGateway.subscriptionQuery(
            ParticipantByProjectQuery(projectId),
            ResponseTypes.multipleInstancesOf(ParticipantQueryResult::class.java),
            ResponseTypes.instanceOf(ParticipantQueryResult::class.java))

    return query
        .initialResult()
        .flatMapMany { Flux.fromIterable(it) }
        .concatWith(query.updates())
        .doFinally { query.cancel() }
  }
}
