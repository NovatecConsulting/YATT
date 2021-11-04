package com.novatecgmbh.eventsourcing.axon.company.employee.command

import com.novatecgmbh.eventsourcing.axon.common.command.AlreadyExistsException
import com.novatecgmbh.eventsourcing.axon.common.command.BaseAggregate
import com.novatecgmbh.eventsourcing.axon.company.company.api.CompanyId
import com.novatecgmbh.eventsourcing.axon.company.employee.api.*
import com.novatecgmbh.eventsourcing.axon.company.employee.command.view.EmployeeUniqueKeyRepository
import com.novatecgmbh.eventsourcing.axon.company.references.ReferenceCheckerService
import com.novatecgmbh.eventsourcing.axon.user.api.UserId
import org.axonframework.commandhandling.CommandHandler
import org.axonframework.eventsourcing.EventSourcingHandler
import org.axonframework.modelling.command.*
import org.axonframework.spring.stereotype.Aggregate
import org.springframework.beans.factory.annotation.Autowired

@Aggregate
class Employee : BaseAggregate() {
  @AggregateIdentifier private lateinit var aggregateIdentifier: EmployeeId
  private lateinit var userId: UserId
  private lateinit var companyId: CompanyId
  private var isAdmin: Boolean = false
  private var isProjectManager: Boolean = false

  @CommandHandler
  @CreationPolicy(AggregateCreationPolicy.CREATE_IF_MISSING)
  fun handle(
      command: CreateEmployeeCommand,
      @Autowired employeeUniqueKeyRepository: EmployeeUniqueKeyRepository,
      @Autowired referenceCheckerService: ReferenceCheckerService
  ): EmployeeId {
    if (::aggregateIdentifier.isInitialized) {
      throw AlreadyExistsException()
    }
    assertNoEmployeeExistsForCompanyAndUser(
        employeeUniqueKeyRepository, command.companyId, command.userId)
    referenceCheckerService.assertUserExists(command.userId.identifier)
    referenceCheckerService.assertCompanyExists(command.companyId.identifier)
    apply(
        EmployeeCreatedEvent(
            aggregateIdentifier = command.aggregateIdentifier,
            userId = command.userId,
            companyId = command.companyId),
        rootContextId = command.companyId.identifier)
    return command.aggregateIdentifier
  }

  private fun assertNoEmployeeExistsForCompanyAndUser(
      employeeUniqueKeyRepository: EmployeeUniqueKeyRepository,
      companyId: CompanyId,
      userId: UserId
  ) {
    if (employeeUniqueKeyRepository.existsByCompanyIdAndUserId(companyId, userId))
        throw IllegalArgumentException("Employee already exists for this company and user")
  }

  @CommandHandler
  fun handle(command: GrantAdminPermissionToEmployee): EmployeeId {
    if (!isAdmin) {
      apply(
          AdminPermissionGrantedForEmployeeEvent(aggregateIdentifier = command.aggregateIdentifier))
    }
    return command.aggregateIdentifier
  }

  @CommandHandler
  fun handle(command: RemoveAdminPermissionFromEmployee): EmployeeId {
    if (isAdmin) {
      apply(
          AdminPermissionRemovedFromEmployeeEvent(
              aggregateIdentifier = command.aggregateIdentifier))
    }
    return command.aggregateIdentifier
  }

  @CommandHandler
  fun handle(command: GrantProjectManagerPermissionToEmployee): EmployeeId {
    if (!isProjectManager) {
      apply(
          ProjectManagerPermissionGrantedForEmployeeEvent(
              aggregateIdentifier = command.aggregateIdentifier))
    }
    return command.aggregateIdentifier
  }

  @CommandHandler
  fun handle(command: RemoveProjectManagerPermissionFromEmployee): EmployeeId {
    if (isProjectManager) {
      apply(
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

  override fun getRootContextId() = companyId.identifier
}
