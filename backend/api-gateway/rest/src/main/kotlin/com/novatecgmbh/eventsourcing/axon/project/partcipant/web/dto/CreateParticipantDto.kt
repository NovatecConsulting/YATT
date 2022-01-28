package com.novatecgmbh.eventsourcing.axon.project.partcipant.web.dto

import com.novatecgmbh.eventsourcing.axon.company.company.api.CompanyId
import com.novatecgmbh.eventsourcing.axon.project.participant.api.CreateParticipantCommand
import com.novatecgmbh.eventsourcing.axon.project.participant.api.ParticipantId
import com.novatecgmbh.eventsourcing.axon.project.project.api.ProjectId
import com.novatecgmbh.eventsourcing.axon.user.api.UserId

data class CreateParticipantDto(
    val projectId: ProjectId,
    val companyId: CompanyId,
    val userId: UserId
) {
  fun toCommand(identifier: ParticipantId) =
      CreateParticipantCommand(identifier, projectId, companyId, userId)
}
