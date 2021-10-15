package com.novatecgmbh.eventsourcing.axon.company.employee.command

import com.novatecgmbh.eventsourcing.axon.common.command.AlreadyExistsException
import com.novatecgmbh.eventsourcing.axon.company.company.api.CompanyId
import com.novatecgmbh.eventsourcing.axon.company.employee.api.*
import com.novatecgmbh.eventsourcing.axon.user.api.UserId
import org.axonframework.commandhandling.CommandHandler
import org.axonframework.eventsourcing.EventSourcingHandler
import org.axonframework.modelling.command.AggregateCreationPolicy
import org.axonframework.modelling.command.AggregateIdentifier
import org.axonframework.modelling.command.AggregateLifecycle
import org.axonframework.modelling.command.CreationPolicy
import org.axonframework.spring.stereotype.Aggregate

@Aggregate
class Employee {
  @AggregateIdentifier private lateinit var aggregateIdentifier: EmployeeId
  private lateinit var userId: UserId
  private lateinit var companyId: CompanyId
  private var isAdmin: Boolean = false
  private var isProjectManager: Boolean = false

  @CommandHandler
  @CreationPolicy(AggregateCreationPolicy.CREATE_IF_MISSING)
  fun handle(command: CreateEmployeeCommand): EmployeeId {
    if (::aggregateIdentifier.isInitialized) {
      throw AlreadyExistsException()
    }
    AggregateLifecycle.apply(
        EmployeeCreatedEvent(
            aggregateIdentifier = command.aggregateIdentifier,
            userId = command.userId,
            companyId = command.companyId))
    return command.aggregateIdentifier
  }

  @CommandHandler
  fun handle(command: GrantAdminPermissionToEmployee): EmployeeId {
    if (!isAdmin) {
      AggregateLifecycle.apply(
          AdminPermissionGrantedForEmployeeEvent(aggregateIdentifier = command.aggregateIdentifier))
    }
    return command.aggregateIdentifier
  }

  @CommandHandler
  fun handle(command: RemoveAdminPermissionFromEmployee): EmployeeId {
    if (isAdmin) {
      AggregateLifecycle.apply(
          AdminPermissionRemovedFromEmployeeEvent(
              aggregateIdentifier = command.aggregateIdentifier))
    }
    return command.aggregateIdentifier
  }

  @CommandHandler
  fun handle(command: GrantProjectManagerPermissionToEmployee): EmployeeId {
    if (!isProjectManager) {
      AggregateLifecycle.apply(
          ProjectManagerPermissionGrantedForEmployeeEvent(
              aggregateIdentifier = command.aggregateIdentifier))
    }
    return command.aggregateIdentifier
  }

  @CommandHandler
  fun handle(command: RemoveProjectManagerPermissionFromEmployee): EmployeeId {
    if (isProjectManager) {
      AggregateLifecycle.apply(
          ProjectManagerPermissionRemovedFromEmployeeEvent(
              aggregateIdentifier = command.aggregateIdentifier))
    }
    return command.aggregateIdentifier
  }

  @EventSourcingHandler
  fun on(event: EmployeeCreatedEvent) {
    aggregateIdentifier = event.aggregateIdentifier
    userId = event.userId
    companyId = event.companyId
  }

  @EventSourcingHandler
  fun on(enum: AdminPermissionGrantedForEmployeeEvent) {
    isAdmin = true
  }

  @EventSourcingHandler
  fun on(enum: AdminPermissionRemovedFromEmployeeEvent) {
    isAdmin = false
  }

  @EventSourcingHandler
  fun on(enum: ProjectManagerPermissionGrantedForEmployeeEvent) {
    isProjectManager = true
  }

  @EventSourcingHandler
  fun on(enum: ProjectManagerPermissionRemovedFromEmployeeEvent) {
    isProjectManager = false
  }
}
