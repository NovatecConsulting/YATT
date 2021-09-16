package com.novatecgmbh.eventsourcing.axon.project.task.query

import com.novatecgmbh.eventsourcing.axon.project.project.command.ProjectId
import com.novatecgmbh.eventsourcing.axon.project.task.command.TaskId
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface TaskProjectionRepository : JpaRepository<TaskProjection, TaskId> {
  fun findAllByProjectId(projectId: ProjectId): MutableIterable<TaskProjection>
}
