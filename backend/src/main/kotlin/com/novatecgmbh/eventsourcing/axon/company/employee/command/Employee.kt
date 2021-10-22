package com.novatecgmbh.eventsourcing.axon.company.employee.command

import com.novatecgmbh.eventsourcing.axon.common.command.AlreadyExistsException
import com.novatecgmbh.eventsourcing.axon.company.company.api.CompanyId
import com.novatecgmbh.eventsourcing.axon.company.company.command.Company
import com.novatecgmbh.eventsourcing.axon.company.employee.api.*
import com.novatecgmbh.eventsourcing.axon.company.employee.command.view.EmployeeUniqueKeyRepository
import com.novatecgmbh.eventsourcing.axon.user.api.UserId
import com.novatecgmbh.eventsourcing.axon.user.command.User
import org.axonframework.commandhandling.CommandHandler
import org.axonframework.eventsourcing.EventSourcingHandler
import org.axonframework.modelling.command.*
import org.axonframework.spring.stereotype.Aggregate
import org.springframework.beans.factory.annotation.Autowired

@Aggregate
class Employee {
  @AggregateIdentifier private lateinit var aggregateIdentifier: EmployeeId
  private lateinit var userId: UserId
  private lateinit var companyId: CompanyId
  private var isAdmin: Boolean = false
  private var isProjectManager: Boolean = false

  @CommandHandler
  @CreationPolicy(AggregateCreationPolicy.CREATE_IF_MISSING)
  fun handle(
      command: CreateEmployeeCommand,
      @Autowired userRepository: Repository<User>,
      @Autowired companyRepository: Repository<Company>,
      @Autowired employeeUniqueKeyRepository: EmployeeUniqueKeyRepository
  ): EmployeeId {
    if (::aggregateIdentifier.isInitialized) {
      throw AlreadyExistsException()
    }
    assertNoEmployeeExistsForCompanyAndUser(
        employeeUniqueKeyRepository, command.companyId, command.userId)
    assertUserExists(userRepository, command.userId)
    assertCompanyExists(companyRepository, command.companyId)
    AggregateLifecycle.apply(
        EmployeeCreatedEvent(
            aggregateIdentifier = command.aggregateIdentifier,
            userId = command.userId,
            companyId = command.companyId))
    return command.aggregateIdentifier
  }

  private fun assertUserExists(userRepository: Repository<User>, userId: UserId) {
    try {
      userRepository.load(userId.identifier)
    } catch (ex: AggregateNotFoundException) {
      throw IllegalArgumentException("Referenced User does not exist")
    }
  }

  private fun assertCompanyExists(companyRepository: Repository<Company>, companyId: CompanyId) {
    try {
      companyRepository.load(companyId.identifier)
    } catch (ex: AggregateNotFoundException) {
      throw IllegalArgumentException("Referenced Company does not exist")
    }
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
