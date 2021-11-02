package com.novatecgmbh.eventsourcing.axon.project.participant.command.eventhandler

import com.novatecgmbh.eventsourcing.axon.application.auditing.AuditUserId
import com.novatecgmbh.eventsourcing.axon.application.auditing.SecurityContextHelper
import com.novatecgmbh.eventsourcing.axon.project.participant.api.CreateParticipantCommand
import com.novatecgmbh.eventsourcing.axon.project.participant.api.ParticipantId
import com.novatecgmbh.eventsourcing.axon.project.project.api.ProjectCreatedEvent
import com.novatecgmbh.eventsourcing.axon.user.api.UserId
import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.config.ProcessingGroup
import org.axonframework.eventhandling.EventHandler
import org.springframework.stereotype.Component

@Component
@ProcessingGroup("participant-auto-creator")
class FirstParticipantAutoCreator(val commandGateway: CommandGateway) {

  @EventHandler
  fun on(event: ProjectCreatedEvent, @AuditUserId userId: String) {
    SecurityContextHelper.setAuthentication(userId)
    commandGateway.send<CreateParticipantCommand>(
        CreateParticipantCommand(
            ParticipantId(), event.aggregateIdentifier, event.companyId, UserId(userId)))
  }
}
