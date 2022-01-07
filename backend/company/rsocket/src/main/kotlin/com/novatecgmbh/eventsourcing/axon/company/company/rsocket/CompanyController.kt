package com.novatecgmbh.eventsourcing.axon.company.company.rsocket

import com.novatecgmbh.eventsourcing.axon.company.company.api.AllCompaniesQuery
import com.novatecgmbh.eventsourcing.axon.company.company.api.CompanyId
import com.novatecgmbh.eventsourcing.axon.company.company.api.CompanyQuery
import com.novatecgmbh.eventsourcing.axon.company.company.api.CompanyQueryResult
import com.novatecgmbh.eventsourcing.axon.company.company.rsocket.dtos.CreateCompanyDto
import org.axonframework.extensions.reactor.commandhandling.gateway.ReactorCommandGateway
import org.axonframework.extensions.reactor.queryhandling.gateway.ReactorQueryGateway
import org.springframework.messaging.handler.annotation.DestinationVariable
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.stereotype.Controller
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Controller
class CompanyController(
    val queryGateway: ReactorQueryGateway,
    val commandGateway: ReactorCommandGateway
) {

  @MessageMapping("companies.create")
  fun createCompany(data: CreateCompanyDto): Mono<CompanyId> = commandGateway.send(data.toCommand())

  @MessageMapping("companies")
  fun subscribeAllCompaniesUpdates(): Flux<CompanyQueryResult> =
      queryGateway.queryUpdates(AllCompaniesQuery(), CompanyQueryResult::class.java)

  @MessageMapping("companies.{id}")
  fun subscribeCompanyByIdUpdates(@DestinationVariable id: CompanyId): Flux<CompanyQueryResult> =
      queryGateway.queryUpdates(CompanyQuery(id), CompanyQueryResult::class.java)
}
