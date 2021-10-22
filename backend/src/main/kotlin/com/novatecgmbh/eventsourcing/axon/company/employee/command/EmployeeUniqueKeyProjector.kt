package com.novatecgmbh.eventsourcing.axon.company.employee.command

import com.novatecgmbh.eventsourcing.axon.company.employee.api.EmployeeCreatedEvent
import org.axonframework.config.ProcessingGroup
import org.axonframework.eventhandling.EventHandler
import org.springframework.stereotype.Component

@Component
@ProcessingGroup("employee-unique-key-projector")
class EmployeeUniqueKeyProjector(private val repository: EmployeeUniqueKeyRepository) {
  @EventHandler
  fun on(event: EmployeeCreatedEvent) {
    repository.save(
        EmployeeUniqueKeyProjection(
            identifier = event.aggregateIdentifier,
            companyId = event.companyId,
            userId = event.userId))
  }
}
