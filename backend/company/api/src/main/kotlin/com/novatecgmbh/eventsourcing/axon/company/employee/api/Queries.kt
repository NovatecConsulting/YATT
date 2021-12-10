package com.novatecgmbh.eventsourcing.axon.company.employee.api

import com.novatecgmbh.eventsourcing.axon.company.company.api.CompanyId
import com.novatecgmbh.eventsourcing.axon.user.api.UserId

class AllEmployeesQuery

data class EmployeesByCompanyQuery(val companyId: CompanyId)

data class EmployeesByMultipleCompaniesQuery(val companyIds: Set<CompanyId>)

data class EmployeeQuery(val employeeId: EmployeeId)

data class EmployeeQueryResult(
    val identifier: EmployeeId,
    val version: Long,
    val companyId: CompanyId,
    val userId: UserId,
    val userFirstName: String? = null,
    val userLastName: String? = null,
    val isAdmin: Boolean = false,
    val isProjectManager: Boolean = false
)
