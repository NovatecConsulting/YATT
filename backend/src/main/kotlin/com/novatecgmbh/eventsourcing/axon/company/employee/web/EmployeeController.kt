package com.novatecgmbh.eventsourcing.axon.company.employee.web

import com.novatecgmbh.eventsourcing.axon.company.company.api.CompanyId
import com.novatecgmbh.eventsourcing.axon.company.employee.api.*
import com.novatecgmbh.eventsourcing.axon.company.employee.web.dto.CreateEmployeeDto
import java.util.concurrent.CompletableFuture
import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.extensions.kotlin.queryMany
import org.axonframework.extensions.kotlin.queryOptional
import org.axonframework.messaging.responsetypes.ResponseTypes
import org.axonframework.queryhandling.QueryGateway
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux

@RestController
class EmployeeController(
    private val commandGateway: CommandGateway,
    private val queryGateway: QueryGateway,
) {
  @PostMapping("/v2/employees")
  fun createEmployee(@RequestBody body: CreateEmployeeDto): CompletableFuture<String> =
      createEmployeeWithId(EmployeeId(), body)

  @PostMapping("/v2/employees/{employeeId}")
  fun createEmployeeWithId(
      @PathVariable("employeeId") employeeId: EmployeeId,
      @RequestBody body: CreateEmployeeDto,
  ): CompletableFuture<String> = commandGateway.send(body.toCommand(employeeId))

  @GetMapping("/v2/employees/{employeeId}")
  fun getEmployeeById(
      @PathVariable("employeeId") employeeId: EmployeeId
  ): ResponseEntity<EmployeeQueryResult> =
      queryGateway
          .queryOptional<EmployeeQueryResult, EmployeeQuery>(EmployeeQuery(employeeId))
          .get()
          .map { ResponseEntity(it, HttpStatus.OK) }
          .orElse(ResponseEntity(HttpStatus.NOT_FOUND))

  @GetMapping("/v2/employees/{employeeId}", produces = [MediaType.APPLICATION_NDJSON_VALUE])
  fun getEmployeeByIdAndUpdates(
      @PathVariable("employeeId") employeeId: EmployeeId
  ): Flux<EmployeeQueryResult> {
    val query =
        queryGateway.subscriptionQuery(
            EmployeeQuery(employeeId),
            ResponseTypes.instanceOf(EmployeeQueryResult::class.java),
            ResponseTypes.instanceOf(EmployeeQueryResult::class.java))

    return query.initialResult().concatWith(query.updates()).doFinally { query.cancel() }
  }

  @GetMapping("/v2/companies/{companyId}/employees")
  fun getEmployeesByCompany(
      @PathVariable("companyId") companyId: CompanyId
  ): ResponseEntity<List<EmployeeQueryResult>> =
      ResponseEntity(
          queryGateway
              .queryMany<EmployeeQueryResult, EmployeesByCompanyQuery>(
                  EmployeesByCompanyQuery(companyId))
              .get(),
          HttpStatus.OK)

  @GetMapping(
      "/v2/companies/{companyId}/employees", produces = [MediaType.APPLICATION_NDJSON_VALUE])
  fun getEmployeesByCompanyAndUpdates(
      @PathVariable("companyId") companyId: CompanyId
  ): Flux<EmployeeQueryResult> {
    val query =
        queryGateway.subscriptionQuery(
            EmployeesByCompanyQuery(companyId),
            ResponseTypes.multipleInstancesOf(EmployeeQueryResult::class.java),
            ResponseTypes.instanceOf(EmployeeQueryResult::class.java))

    return query
        .initialResult()
        .flatMapMany { Flux.fromIterable(it) }
        .concatWith(query.updates())
        .doFinally { query.cancel() }
  }

  @PostMapping("/v2/companies/{employeeId}/permission/admin/grant")
  fun grantAdminPermission(
      @PathVariable("employeeId") employeeId: EmployeeId
  ): CompletableFuture<String> = commandGateway.send(GrantAdminPermissionToEmployee(employeeId))

  @PostMapping("/v2/companies/{employeeId}/permission/admin/remove")
  fun removeAdminPermission(
      @PathVariable("employeeId") employeeId: EmployeeId
  ): CompletableFuture<String> = commandGateway.send(RemoveAdminPermissionFromEmployee(employeeId))

  @PostMapping("/v2/companies/{employeeId}/permission/projectmanager/grant")
  fun grantProjectManagerPermission(
      @PathVariable("employeeId") employeeId: EmployeeId
  ): CompletableFuture<String> =
      commandGateway.send(GrantProjectManagerPermissionToEmployee(employeeId))

  @PostMapping("/v2/companies/{employeeId}/permission/projectmanager/remove")
  fun removeProjectManagerPermission(
      @PathVariable("employeeId") employeeId: EmployeeId
  ): CompletableFuture<String> =
      commandGateway.send(RemoveProjectManagerPermissionFromEmployee(employeeId))
}
