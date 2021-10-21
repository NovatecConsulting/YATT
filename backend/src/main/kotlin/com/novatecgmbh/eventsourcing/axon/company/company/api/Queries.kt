package com.novatecgmbh.eventsourcing.axon.company.company.api

import com.novatecgmbh.eventsourcing.axon.common.query.AggregateReference

class AllCompaniesQuery

data class CompanyQuery(val companyId: CompanyId)

data class CompanyQueryResult(val identifier: CompanyId, val version: Long, val name: String) {
  fun toAggregateReference() = AggregateReference(identifier, name)
}
