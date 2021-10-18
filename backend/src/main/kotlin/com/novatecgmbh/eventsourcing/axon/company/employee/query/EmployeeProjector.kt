package com.novatecgmbh.eventsourcing.axon.company.employee.query

import com.novatecgmbh.eventsourcing.axon.company.employee.api.*
import com.novatecgmbh.eventsourcing.axon.user.api.UserQuery
import com.novatecgmbh.eventsourcing.axon.user.api.UserQueryResult
import java.util.*
import org.axonframework.config.ProcessingGroup
import org.axonframework.eventhandling.EventHandler
import org.axonframework.eventhandling.ResetHandler
import org.axonframework.eventhandling.SequenceNumber
import org.axonframework.extensions.kotlin.emit
import org.axonframework.extensions.kotlin.queryOptional
import org.axonframework.queryhandling.QueryGateway
import org.axonframework.queryhandling.QueryHandler
import org.axonframework.queryhandling.QueryUpdateEmitter
import org.springframework.stereotype.Component

@Component
@ProcessingGroup("employee-projector")
class EmployeeProjector(
    private val repository: EmployeeProjectionRepository,
    private val queryUpdateEmitter: QueryUpdateEmitter,
    private val queryGateway: QueryGateway
) {
  @EventHandler
  fun on(event: EmployeeCreatedEvent, @SequenceNumber aggregateVersion: Long) {
    val user = queryGateway.queryOptional<UserQueryResult, UserQuery>(UserQuery(event.userId)).get()
    saveProjection(
        EmployeeProjection(
            identifier = event.aggregateIdentifier,
            version = aggregateVersion,
            companyId = event.companyId,
            userId = event.userId,
            userFirstName = user.map { it.firstname }.orElse(null),
            userLastName = user.map { it.lastname }.orElse(null)))
  }

  @EventHandler
  fun on(event: AdminPermissionGrantedForEmployeeEvent, @SequenceNumber aggregateVersion: Long) {
    updateProjection(event.aggregateIdentifier) {
      it.isAdmin = true
      it.version = aggregateVersion
    }
  }

  @EventHandler
  fun on(event: AdminPermissionRemovedFromEmployeeEvent, @SequenceNumber aggregateVersion: Long) {
    updateProjection(event.aggregateIdentifier) {
      it.isAdmin = false
      it.version = aggregateVersion
    }
  }

  @EventHandler
  fun on(
      event: ProjectManagerPermissionGrantedForEmployeeEvent,
      @SequenceNumber aggregateVersion: Long
  ) {
    updateProjection(event.aggregateIdentifier) {
      it.isProjectManager = true
      it.version = aggregateVersion
    }
  }

  @EventHandler
  fun on(
      event: ProjectManagerPermissionRemovedFromEmployeeEvent,
      @SequenceNumber aggregateVersion: Long
  ) {
    updateProjection(event.aggregateIdentifier) {
      it.isProjectManager = false
      it.version = aggregateVersion
    }
  }

  private fun updateProjection(identifier: EmployeeId, stateChanges: (EmployeeProjection) -> Unit) {
    repository.findById(identifier).get().also {
      stateChanges.invoke(it)
      saveProjection(it)
    }
  }

  private fun saveProjection(projection: EmployeeProjection) {
    repository.save(projection).also { savedProjection -> updateQuerySubscribers(savedProjection) }
  }

  private fun updateQuerySubscribers(employee: EmployeeProjection) {
    queryUpdateEmitter.emit<EmployeesByCompanyQuery, EmployeeQueryResult>(
        employee.toQueryResult()) { query -> query.companyId == employee.companyId }

    queryUpdateEmitter.emit<EmployeeQuery, EmployeeQueryResult>(employee.toQueryResult()) { true }
  }

  @ResetHandler fun reset() = repository.deleteAll()

  @QueryHandler
  fun handle(query: EmployeeQuery): Optional<EmployeeQueryResult> =
      repository.findById(query.employeeId).map { it.toQueryResult() }

  @QueryHandler
  fun handle(query: EmployeesByCompanyQuery): Iterable<EmployeeQueryResult> =
      repository.findAllByCompanyId(query.companyId).map { it.toQueryResult() }
}
