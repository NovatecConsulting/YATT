package com.novatecgmbh.eventsourcing.axon.company.company.rsocket.dtos

import com.novatecgmbh.eventsourcing.axon.company.company.api.CompanyId
import com.novatecgmbh.eventsourcing.axon.company.company.api.CreateCompanyCommand

data class CreateCompanyDto(val companyId: CompanyId, val name: String) {
  fun toCommand() = CreateCompanyCommand(companyId, name)
}
