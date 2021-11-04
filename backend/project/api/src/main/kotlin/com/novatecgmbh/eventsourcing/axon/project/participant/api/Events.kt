package com.novatecgmbh.eventsourcing.axon.project.participant.api

import com.novatecgmbh.eventsourcing.axon.company.company.api.CompanyId
import com.novatecgmbh.eventsourcing.axon.project.project.api.ProjectId
import com.novatecgmbh.eventsourcing.axon.user.api.UserId

abstract class ParticipantEvent(open val aggregateIdentifier: ParticipantId)

data class ParticipantCreatedEvent(
    override val aggregateIdentifier: ParticipantId,
    val projectId: ProjectId,
    val companyId: CompanyId,
    val userId: UserId
) : ParticipantEvent(aggregateIdentifier)
