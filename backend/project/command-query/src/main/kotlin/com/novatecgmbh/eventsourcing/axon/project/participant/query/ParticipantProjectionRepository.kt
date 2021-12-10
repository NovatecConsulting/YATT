package com.novatecgmbh.eventsourcing.axon.project.participant.query

import com.novatecgmbh.eventsourcing.axon.project.participant.api.ParticipantId
import com.novatecgmbh.eventsourcing.axon.project.project.api.ProjectId
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ParticipantProjectionRepository : JpaRepository<ParticipantProjection, ParticipantId> {
  fun findAllByProjectId(projectId: ProjectId): List<ParticipantProjection>
  fun findAllByProjectIdIn(projectId: Set<ProjectId>): List<ParticipantProjection>
}
