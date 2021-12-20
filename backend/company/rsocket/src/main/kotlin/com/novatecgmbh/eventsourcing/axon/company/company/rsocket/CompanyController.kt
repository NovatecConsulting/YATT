package com.novatecgmbh.eventsourcing.axon.company.company.rsocket

import com.novatecgmbh.eventsourcing.axon.company.company.api.AllCompaniesQuery
import com.novatecgmbh.eventsourcing.axon.company.company.api.CompanyId
import com.novatecgmbh.eventsourcing.axon.company.company.api.CompanyQuery
import com.novatecgmbh.eventsourcing.axon.company.company.api.CompanyQueryResult
import org.axonframework.extensions.reactor.queryhandling.gateway.ReactorQueryGateway
import org.springframework.messaging.handler.annotation.DestinationVariable
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.stereotype.Controller
import reactor.core.publisher.Flux

@Controller
class CompanyController(val queryGateway: ReactorQueryGateway) {

  @MessageMapping("companies")
  fun subscribeAllCompaniesUpdates(): Flux<CompanyQueryResult> =
      queryGateway.queryUpdates(AllCompaniesQuery(), CompanyQueryResult::class.java)

  @MessageMapping("companies.{id}")
  fun subscribeCompanyByIdUpdates(@DestinationVariable id: CompanyId): Flux<CompanyQueryResult> =
      queryGateway.queryUpdates(CompanyQuery(id), CompanyQueryResult::class.java)
}
