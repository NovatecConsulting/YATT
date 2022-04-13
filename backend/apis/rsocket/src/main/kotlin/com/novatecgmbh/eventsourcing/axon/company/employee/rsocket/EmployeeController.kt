package com.novatecgmbh.eventsourcing.axon.company.employee.rsocket

import com.novatecgmbh.eventsourcing.axon.company.company.api.CompanyId
import com.novatecgmbh.eventsourcing.axon.company.employee.api.*
import com.novatecgmbh.eventsourcing.axon.company.employee.rsocket.dtos.CreateEmployeeDto
import org.axonframework.extensions.reactor.commandhandling.gateway.ReactorCommandGateway
import org.axonframework.extensions.reactor.queryhandling.gateway.ReactorQueryGateway
import org.springframework.messaging.handler.annotation.DestinationVariable
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.stereotype.Controller
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Controller
class EmployeeController(
    val queryGateway: ReactorQueryGateway,
    val commandGateway: ReactorCommandGateway
) {

  @MessageMapping("employees.create")
  fun createEmployee(data: CreateEmployeeDto): Mono<EmployeeId> =
      commandGateway.send(data.toCommand())

  @MessageMapping("employees.{id}.permission.admin.grant")
  fun grantAdminPermission(@DestinationVariable id: EmployeeId): Mono<Unit> =
      commandGateway.send(GrantAdminPermissionToEmployee(id))

  @MessageMapping("employees.{id}.permission.admin.remove")
  fun removeAdminPermission(@DestinationVariable id: EmployeeId): Mono<Unit> =
      commandGateway.send(RemoveAdminPermissionFromEmployee(id))

  @MessageMapping("employees.{id}.permission.project-manager.grant")
  fun grantProjectManagerPermission(@DestinationVariable id: EmployeeId): Mono<Unit> =
      commandGateway.send(GrantProjectManagerPermissionToEmployee(id))

  @MessageMapping("employees.{id}.permission.project-manager.remove")
  fun removeProjectManagerPermission(@DestinationVariable id: EmployeeId): Mono<Unit> =
      commandGateway.send(RemoveProjectManagerPermissionFromEmployee(id))

  @MessageMapping("employees.{id}")
  fun subscribeEmployeeByIdUpdates(@DestinationVariable id: EmployeeId): Flux<EmployeeQueryResult> =
      queryGateway.queryUpdates(EmployeeQuery(id), EmployeeQueryResult::class.java)

  @MessageMapping("companies.{id}.employees")
  fun subscribeEmployeeByCompanyUpdates(
      @DestinationVariable id: CompanyId
  ): Flux<EmployeeQueryResult> =
      queryGateway.queryUpdates(EmployeesByCompanyQuery(id), EmployeeQueryResult::class.java)
}
