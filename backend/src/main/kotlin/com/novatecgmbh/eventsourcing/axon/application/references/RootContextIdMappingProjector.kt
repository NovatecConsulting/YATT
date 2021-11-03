package com.novatecgmbh.eventsourcing.axon.application.references

import com.novatecgmbh.eventsourcing.axon.application.sequencing.RootContextId
import com.novatecgmbh.eventsourcing.axon.company.company.api.CompanyCreatedEvent
import com.novatecgmbh.eventsourcing.axon.company.employee.api.EmployeeCreatedEvent
import com.novatecgmbh.eventsourcing.axon.project.participant.api.ParticipantCreatedEvent
import com.novatecgmbh.eventsourcing.axon.project.project.api.ProjectCreatedEvent
import com.novatecgmbh.eventsourcing.axon.project.task.api.TaskCreatedEvent
import com.novatecgmbh.eventsourcing.axon.user.api.UserRegisteredEvent
import org.axonframework.config.ProcessingGroup
import org.axonframework.eventhandling.EventHandler
import org.axonframework.eventhandling.ResetHandler
import org.springframework.stereotype.Component

@Component
@ProcessingGroup("root-context-id-mapping-projector")
class RootContextIdMappingProjector(private val repository: RootContextIdMappingRepository) {

  @EventHandler
  fun on(event: ProjectCreatedEvent, @RootContextId rootContextId: String) =
      addIdMapping("PROJECT", event.aggregateIdentifier.toString(), rootContextId)

  @EventHandler
  fun on(event: TaskCreatedEvent, @RootContextId rootContextId: String) =
      addIdMapping("TASK", event.identifier.toString(), rootContextId)

  @EventHandler
  fun on(event: ParticipantCreatedEvent, @RootContextId rootContextId: String) =
      addIdMapping("PARTICIPANT", event.aggregateIdentifier.toString(), rootContextId)

  @EventHandler
  fun on(event: CompanyCreatedEvent, @RootContextId rootContextId: String) =
      addIdMapping("COMPANY", event.aggregateIdentifier.toString(), rootContextId)

  @EventHandler
  fun on(event: EmployeeCreatedEvent, @RootContextId rootContextId: String) =
      addIdMapping("EMPLOYEE", event.aggregateIdentifier.toString(), rootContextId)

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
