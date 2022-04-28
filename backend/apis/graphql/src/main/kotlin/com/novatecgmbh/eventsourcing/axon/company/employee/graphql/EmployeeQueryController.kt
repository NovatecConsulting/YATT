package com.novatecgmbh.eventsourcing.axon.company.employee.graphql

import com.novatecgmbh.eventsourcing.axon.company.company.api.CompanyQueryResult
import com.novatecgmbh.eventsourcing.axon.company.employee.api.EmployeeId
import com.novatecgmbh.eventsourcing.axon.company.employee.api.EmployeeQuery
import com.novatecgmbh.eventsourcing.axon.company.employee.api.EmployeeQueryResult
import com.novatecgmbh.eventsourcing.axon.company.employee.api.EmployeesByMultipleCompaniesQuery
import java.util.concurrent.CompletableFuture
import org.axonframework.extensions.kotlin.queryMany
import org.axonframework.extensions.kotlin.queryOptional
import org.axonframework.queryhandling.QueryGateway
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.BatchMapping
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.stereotype.Controller
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

@Controller
class EmployeeQueryController(val queryGateway: QueryGateway) {

  @QueryMapping
  fun employee(@Argument identifier: EmployeeId): CompletableFuture<EmployeeQueryResult?> =
      queryGateway.queryOptional<EmployeeQueryResult, EmployeeQuery>(EmployeeQuery(identifier))
          .thenApply { optional -> optional.orElse(null) }

  @BatchMapping(typeName = "Company")
  fun employees(
      companies: Set<CompanyQueryResult>
  ): Mono<Map<CompanyQueryResult, List<EmployeeQueryResult>>> =
      queryGateway
          .queryMany<EmployeeQueryResult, EmployeesByMultipleCompaniesQuery>(
              EmployeesByMultipleCompaniesQuery(
                  companies.map(CompanyQueryResult::identifier).toSet()))
          .thenApply {
            it.groupBy { employee ->
              companies.first { company -> company.identifier == employee.companyId }
            }
          }
          .toMono()
}
