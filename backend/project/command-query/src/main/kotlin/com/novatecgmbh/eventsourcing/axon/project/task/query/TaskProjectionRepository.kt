package com.novatecgmbh.eventsourcing.axon.project.task.query

import com.novatecgmbh.eventsourcing.axon.project.project.api.ProjectId
import com.novatecgmbh.eventsourcing.axon.project.task.api.TaskId
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface TaskProjectionRepository : JpaRepository<TaskProjection, TaskId> {
  @Query("select t from TaskProjection t left join fetch t.todos where t.projectId = :projectId")
  fun findAllByProjectId(@Param("projectId") projectId: ProjectId): List<TaskProjection>

  @Query("select t from TaskProjection t left join fetch t.todos where t.projectId in :projectIds")
  fun findAllByProjectIdIn(@Param("projectIds") projectIds: Set<ProjectId>): List<TaskProjection>
}
