package com.novatecgmbh.eventsourcing.axon.project.participant.graphql

import com.novatecgmbh.eventsourcing.axon.company.company.api.CompanyId
import com.novatecgmbh.eventsourcing.axon.project.participant.api.CreateParticipantCommand
import com.novatecgmbh.eventsourcing.axon.project.participant.api.ParticipantId
import com.novatecgmbh.eventsourcing.axon.project.project.api.ProjectId
import com.novatecgmbh.eventsourcing.axon.user.api.UserId
import java.util.concurrent.CompletableFuture
import org.axonframework.commandhandling.gateway.CommandGateway
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.MutationMapping
import org.springframework.stereotype.Controller

@Controller
class ParticipantMutationsController(val commandGateway: CommandGateway) {

  @MutationMapping
  fun createParticipant(
      @Argument projectIdentifier: ProjectId,
      @Argument companyIdentifier: CompanyId,
      @Argument userIdentifier: UserId
  ): CompletableFuture<ParticipantId> =
      commandGateway.send(
          CreateParticipantCommand(
              ParticipantId(), projectIdentifier, companyIdentifier, userIdentifier))
}
