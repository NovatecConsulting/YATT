package com.novatecgmbh.eventsourcing.axon.company.employee.api

import com.novatecgmbh.eventsourcing.axon.company.company.api.CompanyId
import com.novatecgmbh.eventsourcing.axon.user.api.UserId

abstract class EmployeeEvent(open val aggregateIdentifier: EmployeeId)

data class EmployeeCreatedEvent(
    override val aggregateIdentifier: EmployeeId,
    val companyId: CompanyId,
    val userId: UserId,
) : EmployeeEvent(aggregateIdentifier)

data class AdminPermissionGrantedForEmployeeEvent(
    override val aggregateIdentifier: EmployeeId,
) : EmployeeEvent(aggregateIdentifier)

data class AdminPermissionRemovedFromEmployeeEvent(
    override val aggregateIdentifier: EmployeeId,
) : EmployeeEvent(aggregateIdentifier)

data class ProjectManagerPermissionGrantedForEmployeeEvent(
    override val aggregateIdentifier: EmployeeId,
) : EmployeeEvent(aggregateIdentifier)

data class ProjectManagerPermissionRemovedFromEmployeeEvent(
    override val aggregateIdentifier: EmployeeId,
) : EmployeeEvent(aggregateIdentifier)
