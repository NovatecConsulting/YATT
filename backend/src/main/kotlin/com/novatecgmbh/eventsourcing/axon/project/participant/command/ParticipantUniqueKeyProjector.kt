package com.novatecgmbh.eventsourcing.axon.project.participant.command

import com.novatecgmbh.eventsourcing.axon.project.participant.api.ParticipantCreatedEvent
import org.axonframework.config.ProcessingGroup
import org.axonframework.eventhandling.EventHandler
import org.springframework.stereotype.Component

@Component
@ProcessingGroup("participant-unique-key-projector")
class ParticipantUniqueKeyProjector(private val repository: ParticipantUniqueKeyRepository) {
  @EventHandler
  fun on(event: ParticipantCreatedEvent) {
    repository.save(
        ParticipantUniqueKeyProjection(
            identifier = event.aggregateIdentifier,
            projectId = event.projectId,
            companyId = event.companyId,
            userId = event.userId))
  }
}
