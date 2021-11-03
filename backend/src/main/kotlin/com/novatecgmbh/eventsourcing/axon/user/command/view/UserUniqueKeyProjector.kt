package com.novatecgmbh.eventsourcing.axon.user.command.view

import com.novatecgmbh.eventsourcing.axon.user.api.UserRegisteredEvent
import org.axonframework.config.ProcessingGroup
import org.axonframework.eventhandling.EventHandler
import org.springframework.stereotype.Component

@Component
@ProcessingGroup("user-unique-key-projector")
class UserUniqueKeyProjector(private val repository: UserUniqueKeyRepository) {
  @EventHandler
  fun on(event: UserRegisteredEvent) {
    repository.save(
        UserUniqueKeyProjection(
            identifier = event.aggregateIdentifier, externalUserId = event.externalUserId))
  }
}
