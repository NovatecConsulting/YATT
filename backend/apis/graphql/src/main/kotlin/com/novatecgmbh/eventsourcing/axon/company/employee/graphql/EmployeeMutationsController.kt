package com.novatecgmbh.eventsourcing.axon.company.employee.graphql

import com.novatecgmbh.eventsourcing.axon.company.company.api.CompanyId
import com.novatecgmbh.eventsourcing.axon.company.employee.api.*
import com.novatecgmbh.eventsourcing.axon.user.api.UserId
import java.util.*
import java.util.concurrent.CompletableFuture
import org.axonframework.commandhandling.gateway.CommandGateway
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.MutationMapping
import org.springframework.stereotype.Controller

@Controller
class EmployeeMutationsController(val commandGateway: CommandGateway) {

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
