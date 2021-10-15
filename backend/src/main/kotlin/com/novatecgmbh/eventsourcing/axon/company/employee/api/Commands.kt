package com.novatecgmbh.eventsourcing.axon.company.employee.api

import com.novatecgmbh.eventsourcing.axon.company.company.api.CompanyId
import com.novatecgmbh.eventsourcing.axon.user.api.UserId
import org.axonframework.modelling.command.TargetAggregateIdentifier

abstract class EmployeeCommand(
    @TargetAggregateIdentifier open val aggregateIdentifier: EmployeeId,
)

data class CreateEmployeeCommand(
    @TargetAggregateIdentifier override val aggregateIdentifier: EmployeeId,
    val companyId: CompanyId,
    val userId: UserId,
) : EmployeeCommand(aggregateIdentifier)

data class GrantAdminPermissionToEmployee(
    @TargetAggregateIdentifier override val aggregateIdentifier: EmployeeId
) : EmployeeCommand(aggregateIdentifier)

data class RemoveAdminPermissionFromEmployee(
    @TargetAggregateIdentifier override val aggregateIdentifier: EmployeeId
) : EmployeeCommand(aggregateIdentifier)

data class GrantProjectManagerPermissionToEmployee(
    @TargetAggregateIdentifier override val aggregateIdentifier: EmployeeId
) : EmployeeCommand(aggregateIdentifier)

data class RemoveProjectManagerPermissionFromEmployee(
    @TargetAggregateIdentifier override val aggregateIdentifier: EmployeeId
) : EmployeeCommand(aggregateIdentifier)
