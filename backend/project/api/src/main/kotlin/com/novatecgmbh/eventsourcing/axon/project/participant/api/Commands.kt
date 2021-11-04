package com.novatecgmbh.eventsourcing.axon.project.participant.api

import com.novatecgmbh.eventsourcing.axon.company.company.api.CompanyId
import com.novatecgmbh.eventsourcing.axon.project.project.api.ProjectId
import com.novatecgmbh.eventsourcing.axon.user.api.UserId
import org.axonframework.modelling.command.TargetAggregateIdentifier

abstract class ParticipantCommand(
    @TargetAggregateIdentifier open val aggregateIdentifier: ParticipantId,
)

data class CreateParticipantCommand(
    @TargetAggregateIdentifier override val aggregateIdentifier: ParticipantId,
    val projectId: ProjectId,
    val companyId: CompanyId,
    val userId: UserId,
) : ParticipantCommand(aggregateIdentifier)
