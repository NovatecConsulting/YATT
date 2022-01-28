package com.novatecgmbh.eventsourcing.axon.company.company.query

import com.novatecgmbh.eventsourcing.axon.company.company.api.AllCompaniesQuery
import com.novatecgmbh.eventsourcing.axon.company.company.api.CompanyQuery
import com.novatecgmbh.eventsourcing.axon.company.company.api.CompanyQueryResult
import java.util.*
import org.axonframework.queryhandling.QueryHandler
import org.springframework.stereotype.Component

@Component
class CompanyQueryHandler(val repository: CompanyProjectionRepository) {

  @QueryHandler
  fun handle(query: CompanyQuery): Optional<CompanyQueryResult> =
      repository.findById(query.companyId).map { it.toQueryResult() }

  @QueryHandler
  fun handle(query: AllCompaniesQuery): Iterable<CompanyQueryResult> =
      repository.findAll().map { it.toQueryResult() }
}
