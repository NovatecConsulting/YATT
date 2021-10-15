package com.novatecgmbh.eventsourcing.axon.company.company.api

class AllCompaniesQuery

data class CompanyQuery(val companyId: CompanyId)

data class CompanyQueryResult(val identifier: CompanyId, val version: Long, val name: String)
