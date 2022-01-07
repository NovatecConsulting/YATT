package com.novatecgmbh.eventsourcing.axon.project.participants.rsocket

import com.novatecgmbh.eventsourcing.axon.project.participant.api.*
import com.novatecgmbh.eventsourcing.axon.project.participants.rsocket.dtos.CreateParticipantDto
import com.novatecgmbh.eventsourcing.axon.project.project.api.ProjectId
import org.axonframework.extensions.reactor.commandhandling.gateway.ReactorCommandGateway
import org.axonframework.extensions.reactor.queryhandling.gateway.ReactorQueryGateway
import org.springframework.messaging.handler.annotation.DestinationVariable
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.stereotype.Controller
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Controller
class ParticipantController(
    val queryGateway: ReactorQueryGateway,
    val commandGateway: ReactorCommandGateway
) {

  @MessageMapping("participants.create")
  fun createParticipant(data: CreateParticipantDto): Mono<ParticipantId> =
      commandGateway.send(data.toCommand())

  @MessageMapping("participants.{id}")
  fun subscribeParticipantByIdUpdates(
      @DestinationVariable id: ParticipantId
  ): Flux<ParticipantQueryResult> =
      queryGateway.queryUpdates(ParticipantQuery(id), ParticipantQueryResult::class.java)

  @MessageMapping("projects.{id}.participants")
  fun subscribeParticipantsByProjectUpdates(
      @DestinationVariable id: ProjectId
  ): Flux<ParticipantQueryResult> =
      queryGateway.queryUpdates(ParticipantByProjectQuery(id), ParticipantQueryResult::class.java)
}
