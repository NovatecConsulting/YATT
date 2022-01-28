package com.novatecgmbh.eventsourcing.axon.company.references

import com.novatecgmbh.eventsourcing.axon.application.sequencing.RootContextId
import com.novatecgmbh.eventsourcing.axon.common.references.RootContextIdMapping
import com.novatecgmbh.eventsourcing.axon.common.references.RootContextIdMappingKey
import com.novatecgmbh.eventsourcing.axon.company.company.api.CompanyCreatedEvent
import com.novatecgmbh.eventsourcing.axon.user.api.UserRegisteredEvent
import org.axonframework.config.ProcessingGroup
import org.axonframework.eventhandling.EventHandler
import org.axonframework.eventhandling.ResetHandler
import org.springframework.stereotype.Component

@Component
@ProcessingGroup("root-context-id-mapping-projector-company")
class RootContextIdMappingProjector(private val repository: RootContextIdMappingRepository) {

  @EventHandler
  fun on(event: CompanyCreatedEvent, @RootContextId rootContextId: String) =
      addIdMapping("COMPANY", event.aggregateIdentifier.toString(), rootContextId)

  @EventHandler
  fun on(event: UserRegisteredEvent, @RootContextId rootContextId: String) =
      addIdMapping("USER", event.aggregateIdentifier.toString(), rootContextId)

  fun addIdMapping(aggregateType: String, aggregateIdentifier: String, rootContextId: String) =
      repository.save(
          RootContextIdMapping(
              RootContextIdMappingKey(aggregateType, aggregateIdentifier, rootContextId)))

  @ResetHandler
  fun reset() {
    repository.deleteAll()
  }
}
