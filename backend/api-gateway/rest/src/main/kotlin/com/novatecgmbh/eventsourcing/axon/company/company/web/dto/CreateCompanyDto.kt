package com.novatecgmbh.eventsourcing.axon.company.company.web.dto

import com.novatecgmbh.eventsourcing.axon.company.company.api.CompanyId
import com.novatecgmbh.eventsourcing.axon.company.company.api.CreateCompanyCommand

data class CreateCompanyDto(val name: String) {
  fun toCommand(companyId: CompanyId) = CreateCompanyCommand(companyId, name)
}
