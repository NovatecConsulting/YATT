package com.novatecgmbh.eventsourcing.axon.company.company.graphql

import com.novatecgmbh.eventsourcing.axon.company.company.api.CompanyId
import com.novatecgmbh.eventsourcing.axon.company.company.api.CreateCompanyCommand
import java.util.concurrent.CompletableFuture
import org.axonframework.commandhandling.gateway.CommandGateway
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.MutationMapping
import org.springframework.stereotype.Controller

@Controller
class CompanyMutationsController(val commandGateway: CommandGateway) {

  @MutationMapping
  fun createCompany(@Argument name: String): CompletableFuture<CompanyId> =
      commandGateway.send(CreateCompanyCommand(CompanyId(), name))
}
