package com.novatecgmbh.eventsourcing.axon.project.participants.rsocket

import com.novatecgmbh.eventsourcing.axon.project.participant.api.ParticipantByProjectQuery
import com.novatecgmbh.eventsourcing.axon.project.participant.api.ParticipantId
import com.novatecgmbh.eventsourcing.axon.project.participant.api.ParticipantQuery
import com.novatecgmbh.eventsourcing.axon.project.participant.api.ParticipantQueryResult
import com.novatecgmbh.eventsourcing.axon.project.project.api.ProjectId
import org.axonframework.extensions.reactor.queryhandling.gateway.ReactorQueryGateway
import org.springframework.messaging.handler.annotation.DestinationVariable
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.stereotype.Controller
import reactor.core.publisher.Flux

@Controller
class ParticipantController(val queryGateway: ReactorQueryGateway) {

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
