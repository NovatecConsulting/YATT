package com.novatecgmbh.eventsourcing.axon.project.participant.command

import com.novatecgmbh.eventsourcing.axon.common.command.AlreadyExistsException
import com.novatecgmbh.eventsourcing.axon.project.participant.api.CreateParticipantCommand
import com.novatecgmbh.eventsourcing.axon.project.participant.api.ParticipantCreatedEvent
import com.novatecgmbh.eventsourcing.axon.project.participant.api.ParticipantId
import com.novatecgmbh.eventsourcing.axon.project.project.api.ProjectId
import com.novatecgmbh.eventsourcing.axon.user.api.UserId
import org.axonframework.commandhandling.CommandHandler
import org.axonframework.eventsourcing.EventSourcingHandler
import org.axonframework.modelling.command.AggregateCreationPolicy
import org.axonframework.modelling.command.AggregateIdentifier
import org.axonframework.modelling.command.AggregateLifecycle
import org.axonframework.modelling.command.CreationPolicy
import org.axonframework.spring.stereotype.Aggregate

@Aggregate
class Participant {
  @AggregateIdentifier private lateinit var aggregateIdentifier: ParticipantId
  private lateinit var projectId: ProjectId
  private lateinit var userId: UserId

  @CommandHandler
  @CreationPolicy(AggregateCreationPolicy.CREATE_IF_MISSING)
  fun handle(command: CreateParticipantCommand): ParticipantId {
    if (::aggregateIdentifier.isInitialized) {
      throw AlreadyExistsException()
    }
    AggregateLifecycle.apply(
        ParticipantCreatedEvent(
            aggregateIdentifier = command.aggregateIdentifier,
            projectId = command.projectId,
            companyId = command.companyId,
            userId = command.userId))
    return command.aggregateIdentifier
  }

  @EventSourcingHandler
  fun on(event: ParticipantCreatedEvent) {
    aggregateIdentifier = event.aggregateIdentifier
    projectId = event.projectId
    userId = event.userId
  }
}
