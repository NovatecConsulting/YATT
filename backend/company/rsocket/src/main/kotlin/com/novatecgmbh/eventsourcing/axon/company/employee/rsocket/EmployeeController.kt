package com.novatecgmbh.eventsourcing.axon.company.employee.rsocket

import com.novatecgmbh.eventsourcing.axon.company.company.api.CompanyId
import com.novatecgmbh.eventsourcing.axon.company.employee.api.EmployeeId
import com.novatecgmbh.eventsourcing.axon.company.employee.api.EmployeeQuery
import com.novatecgmbh.eventsourcing.axon.company.employee.api.EmployeeQueryResult
import com.novatecgmbh.eventsourcing.axon.company.employee.api.EmployeesByCompanyQuery
import org.axonframework.extensions.reactor.queryhandling.gateway.ReactorQueryGateway
import org.springframework.messaging.handler.annotation.DestinationVariable
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.stereotype.Controller
import reactor.core.publisher.Flux

@Controller
class EmployeeController(val queryGateway: ReactorQueryGateway) {

  @MessageMapping("employees.{id}")
  fun subscribeEmployeeByIdUpdates(@DestinationVariable id: EmployeeId): Flux<EmployeeQueryResult> =
      queryGateway.queryUpdates(EmployeeQuery(id), EmployeeQueryResult::class.java)

  @MessageMapping("companies.{id}.employees")
  fun subscribeEmployeeByCompanyUpdates(
      @DestinationVariable id: CompanyId
  ): Flux<EmployeeQueryResult> =
      queryGateway.queryUpdates(EmployeesByCompanyQuery(id), EmployeeQueryResult::class.java)
}
