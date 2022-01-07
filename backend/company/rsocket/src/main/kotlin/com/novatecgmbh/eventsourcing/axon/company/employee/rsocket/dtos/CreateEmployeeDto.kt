package com.novatecgmbh.eventsourcing.axon.company.employee.rsocket.dtos

import com.novatecgmbh.eventsourcing.axon.company.company.api.CompanyId
import com.novatecgmbh.eventsourcing.axon.company.employee.api.CreateEmployeeCommand
import com.novatecgmbh.eventsourcing.axon.company.employee.api.EmployeeId
import com.novatecgmbh.eventsourcing.axon.user.api.UserId

data class CreateEmployeeDto(
    val identifier: EmployeeId = EmployeeId(),
    val companyId: CompanyId,
    val userId: UserId
) {
  fun toCommand() = CreateEmployeeCommand(identifier, companyId, userId)
}
