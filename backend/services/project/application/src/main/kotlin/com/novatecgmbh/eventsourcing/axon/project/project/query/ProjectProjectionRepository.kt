package com.novatecgmbh.eventsourcing.axon.project.project.query

import com.novatecgmbh.eventsourcing.axon.project.project.api.ProjectId
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ProjectProjectionRepository : JpaRepository<ProjectProjection, ProjectId> {
  fun findAllByIdentifierInOrderByName(projectIds: Collection<ProjectId>): List<ProjectProjection>
}
