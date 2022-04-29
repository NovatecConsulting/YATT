package com.novatecgmbh.eventsourcing.axon.project.participant.api

import com.novatecgmbh.eventsourcing.axon.company.company.api.CompanyId
import com.novatecgmbh.eventsourcing.axon.project.project.api.ProjectId
import com.novatecgmbh.eventsourcing.axon.user.api.UserId

data class ParticipantByProjectQuery(val projectId: ProjectId)

data class ParticipantByMultipleProjectsQuery(val projectIds: Set<ProjectId>)

data class ParticipantQuery(val participantId: ParticipantId)

data class ParticipantQueryResult(
    val identifier: ParticipantId,
    val version: Long,
    val projectId: ProjectId,
    val companyId: CompanyId,
    val companyName: String?,
    val userId: UserId,
    val userFirstName: String?,
    val userLastName: String?,
    val userEmail: String?,
    val userTelephone: String?,
)
