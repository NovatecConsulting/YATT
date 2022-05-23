package com.novatecgmbh.eventsourcing.axon.project.task.query

import com.novatecgmbh.eventsourcing.axon.project.project.api.ProjectId
import com.novatecgmbh.eventsourcing.axon.project.task.api.TaskId
import java.time.LocalDate
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface TaskProjectionRepository : JpaRepository<TaskProjection, TaskId> {
  @Query(
      "select t from TaskProjection t left join fetch t.todos where t.projectId = :projectId order by t.startDate, t.name")
  fun findAllByProjectId(@Param("projectId") projectId: ProjectId): List<TaskProjection>

  @Query(
      "select t from TaskProjection t left join fetch t.todos " +
          "where t.projectId in :projectIds " +
          "and (cast(:from as date) is null or t.startDate > :from or t.endDate > :from) " +
          "and (cast(:to as date) is null or t.startDate < :to or t.endDate < :to) order by t.startDate, t.name")
  fun findAllByProjectIdInAndDatesInRange(
      @Param("projectIds") projectIds: Set<ProjectId>,
      @Param("from") from: LocalDate?,
      @Param("to") to: LocalDate?
  ): List<TaskProjection>
}
