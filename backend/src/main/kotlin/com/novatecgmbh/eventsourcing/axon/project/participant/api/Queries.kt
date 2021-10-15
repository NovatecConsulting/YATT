package com.novatecgmbh.eventsourcing.axon.project.participant.api

import com.novatecgmbh.eventsourcing.axon.company.company.api.CompanyId
import com.novatecgmbh.eventsourcing.axon.company.employee.api.EmployeeId
import com.novatecgmbh.eventsourcing.axon.project.project.api.ProjectId
import com.novatecgmbh.eventsourcing.axon.user.api.UserId

data class ParticipantByProjectQuery(val projectId: ProjectId)

data class ParticipantQuery(val participantId: ParticipantId)

data class ParticipantQueryResult(
    val identifier: EmployeeId,
    val version: Long,
    val companyId: CompanyId,
    val userId: UserId,
    val userFirstName: String?,
    val userLastName: String?
)
