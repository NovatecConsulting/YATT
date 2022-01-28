package com.novatecgmbh.eventsourcing.axon.company.employee.web.dto

import com.novatecgmbh.eventsourcing.axon.company.company.api.CompanyId
import com.novatecgmbh.eventsourcing.axon.company.employee.api.CreateEmployeeCommand
import com.novatecgmbh.eventsourcing.axon.company.employee.api.EmployeeId
import com.novatecgmbh.eventsourcing.axon.user.api.UserId

data class CreateEmployeeDto(val companyId: CompanyId, val userId: UserId) {
  fun toCommand(identifier: EmployeeId) = CreateEmployeeCommand(identifier, companyId, userId)
}
