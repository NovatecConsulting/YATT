package com.novatecgmbh.eventsourcing.axon.company.company.query

import com.novatecgmbh.eventsourcing.axon.company.company.api.*
import org.axonframework.config.ProcessingGroup
import org.axonframework.eventhandling.EventHandler
import org.axonframework.eventhandling.ResetHandler
import org.axonframework.eventhandling.SequenceNumber
import org.axonframework.extensions.kotlin.emit
import org.axonframework.queryhandling.QueryUpdateEmitter
import org.springframework.stereotype.Component

@Component
@ProcessingGroup("company-projector")
class CompanyProjector(
    private val repository: CompanyProjectionRepository,
    private val queryUpdateEmitter: QueryUpdateEmitter
) {
  @EventHandler
  fun on(event: CompanyCreatedEvent, @SequenceNumber aggregateVersion: Long) {
    saveProjection(
        CompanyProjection(
            identifier = event.aggregateIdentifier, version = aggregateVersion, name = event.name))
  }

  private fun updateProjection(identifier: CompanyId, stateChanges: (CompanyProjection) -> Unit) {
    repository.findById(identifier).get().also {
      stateChanges.invoke(it)
      saveProjection(it)
    }
  }

  private fun saveProjection(projection: CompanyProjection) {
    repository.save(projection).also { savedProjection -> updateQuerySubscribers(savedProjection) }
  }

  private fun updateQuerySubscribers(company: CompanyProjection) {
    queryUpdateEmitter.emit<CompanyQuery, CompanyQueryResult>(company.toQueryResult()) { query ->
      query.companyId == company.identifier
    }

    queryUpdateEmitter.emit<AllCompaniesQuery, CompanyQueryResult>(company.toQueryResult()) { true }
  }

  @ResetHandler fun reset() = repository.deleteAll()
}
