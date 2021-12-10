package com.novatecgmbh.eventsourcing.axon.controller

import com.novatecgmbh.eventsourcing.axon.company.company.api.CompanyId
import com.novatecgmbh.eventsourcing.axon.company.company.api.CompanyQueryResult
import com.novatecgmbh.eventsourcing.axon.company.employee.api.*
import com.novatecgmbh.eventsourcing.axon.user.api.UserId
import jdk.jfr.StackTrace
import java.util.*
import java.util.concurrent.CompletableFuture
import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.extensions.kotlin.queryMany
import org.axonframework.extensions.kotlin.queryOptional
import org.axonframework.queryhandling.QueryGateway
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.BatchMapping
import org.springframework.graphql.data.method.annotation.MutationMapping
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.stereotype.Controller
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

@Controller
class EmployeeController(val commandGateway: CommandGateway, val queryGateway: QueryGateway) {

  @QueryMapping
  fun employee(@Argument identifier: EmployeeId): CompletableFuture<EmployeeQueryResult?> =
      queryGateway.queryOptional<EmployeeQueryResult, EmployeeQuery>(EmployeeQuery(identifier))
          .thenApply { optional -> optional.orElse(null) }

  @BatchMapping
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

  @MutationMapping
  fun createEmployee(
      @Argument companyId: CompanyId,
      @Argument userId: UserId
  ): CompletableFuture<UUID> =
      commandGateway.send(CreateEmployeeCommand(EmployeeId(), companyId, userId))

  @MutationMapping
  fun grantProjectManagerPermissionToEmployee(
      @Argument identifier: EmployeeId
  ): CompletableFuture<Long> =
      commandGateway.send(GrantProjectManagerPermissionToEmployee(identifier))

  @MutationMapping
  fun removeProjectManagerPermissionFromEmployee(
      @Argument identifier: EmployeeId
  ): CompletableFuture<Long> =
      commandGateway.send(RemoveProjectManagerPermissionFromEmployee(identifier))

  @MutationMapping
  fun grantAdminPermissionToEmployee(@Argument identifier: EmployeeId): CompletableFuture<Long> =
      commandGateway.send(GrantAdminPermissionToEmployee(identifier))

  @MutationMapping
  fun removeAdminPermissionFromEmployee(@Argument identifier: EmployeeId): CompletableFuture<Long> =
      commandGateway.send(RemoveAdminPermissionFromEmployee(identifier))
}
