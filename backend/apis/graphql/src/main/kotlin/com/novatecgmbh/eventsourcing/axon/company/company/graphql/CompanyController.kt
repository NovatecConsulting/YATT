package com.novatecgmbh.eventsourcing.axon.company.company.graphql

import com.novatecgmbh.eventsourcing.axon.company.company.api.*
import java.util.concurrent.CompletableFuture
import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.extensions.kotlin.queryMany
import org.axonframework.extensions.kotlin.queryOptional
import org.axonframework.queryhandling.QueryGateway
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.MutationMapping
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.stereotype.Controller

@Controller
class CompanyController(val commandGateway: CommandGateway, val queryGateway: QueryGateway) {

  @QueryMapping
  fun company(@Argument identifier: CompanyId): CompletableFuture<CompanyQueryResult?> =
      queryGateway.queryOptional<CompanyQueryResult, CompanyQuery>(CompanyQuery(identifier))
          .thenApply { optional -> optional.orElse(null) }

  @QueryMapping
  fun companies(): CompletableFuture<List<CompanyQueryResult>> =
      queryGateway.queryMany(AllCompaniesQuery())

  @MutationMapping
  fun createCompany(@Argument name: String): CompletableFuture<CompanyId> =
      commandGateway.send(CreateCompanyCommand(CompanyId(), name))
}
