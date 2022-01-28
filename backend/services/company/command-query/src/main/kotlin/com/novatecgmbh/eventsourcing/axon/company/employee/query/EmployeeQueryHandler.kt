package com.novatecgmbh.eventsourcing.axon.company.employee.query

import com.novatecgmbh.eventsourcing.axon.company.employee.api.*
import java.util.*
import org.axonframework.queryhandling.QueryHandler
import org.springframework.stereotype.Component

@Component
class EmployeeQueryHandler(val repository: EmployeeProjectionRepository) {

  @QueryHandler
  fun handle(query: EmployeeQuery): Optional<EmployeeQueryResult> =
      repository.findById(query.employeeId).map { it.toQueryResult() }

  @QueryHandler
  fun handle(query: EmployeesByCompanyQuery): Iterable<EmployeeQueryResult> =
      repository.findAllByCompanyId(query.companyId).map { it.toQueryResult() }

  @QueryHandler
  fun handle(query: EmployeesByMultipleCompaniesQuery): Iterable<EmployeeQueryResult> =
      repository.findAllByCompanyIdIn(query.companyIds).map { it.toQueryResult() }

  @QueryHandler
  fun handle(query: AllEmployeesQuery): Iterable<EmployeeQueryResult> =
      repository.findAll().map { it.toQueryResult() }
}
