package com.novatecgmbh.eventsourcing.axon.project.project.query

import com.novatecgmbh.eventsourcing.axon.project.project.api.*
import com.novatecgmbh.eventsourcing.axon.project.task.api.TaskCompletedEvent
import com.novatecgmbh.eventsourcing.axon.project.task.api.TaskCreatedEvent
import com.novatecgmbh.eventsourcing.axon.project.task.api.TaskId
import com.novatecgmbh.eventsourcing.axon.project.task.api.TaskStartedEvent
import java.time.LocalDate
import java.util.*
import javax.persistence.*
import org.axonframework.config.ProcessingGroup
import org.axonframework.eventhandling.EventHandler
import org.axonframework.eventhandling.ResetHandler
import org.axonframework.eventhandling.SequenceNumber
import org.axonframework.extensions.kotlin.emit
import org.axonframework.queryhandling.QueryUpdateEmitter
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Component
import org.springframework.stereotype.Repository

@Entity
@Table(name = "project_details")
class ProjectDetailsProjection(
    @EmbeddedId var identifier: ProjectId,
    @Column(nullable = false) var version: Long,
    @Column(nullable = false) var name: String,
    @Column(nullable = false) var plannedStartDate: LocalDate,
    @Column(nullable = false) var deadline: LocalDate,
    @Column(nullable = false) var allTasksCount: Long,
    @Column(nullable = false) var plannedTasksCount: Long,
    @Column(nullable = false) var startedTasksCount: Long,
    @Column(nullable = false) var completedTasksCount: Long,
) {
  fun toQueryResult() =
      ProjectDetailsQueryResult(
          identifier,
          version,
          name,
          plannedStartDate,
          deadline,
          allTasksCount,
          plannedTasksCount,
          startedTasksCount,
          completedTasksCount)
}

@Entity
@Table(name = "project_by_task_lookup")
class ProjectByTaskLookupProjection(
    @EmbeddedId var taskId: TaskId,
    @Embedded
    @AttributeOverride(name = "identifier", column = Column(name = "projectId", nullable = false))
    var projectId: ProjectId,
)

@Repository
interface ProjectByTaskLookupProjectionRepository :
    JpaRepository<ProjectByTaskLookupProjection, TaskId>

@Repository
interface ProjectDetailsProjectionRepository : JpaRepository<ProjectDetailsProjection, ProjectId>

@Component
@ProcessingGroup("project-details-projector")
class ProjectDetailsProjector(
    private val repository: ProjectDetailsProjectionRepository,
    private val lookupRepository: ProjectByTaskLookupProjectionRepository,
    private val queryUpdateEmitter: QueryUpdateEmitter
) {
  @EventHandler
  fun on(event: ProjectCreatedEvent, @SequenceNumber aggregateVersion: Long) {
    saveProjection(
        ProjectDetailsProjection(
            identifier = event.aggregateIdentifier,
            version = aggregateVersion,
            name = event.projectName,
            plannedStartDate = event.plannedStartDate,
            deadline = event.deadline,
            allTasksCount = 0,
            plannedTasksCount = 0,
            startedTasksCount = 0,
            completedTasksCount = 0,
        ))
  }

  @EventHandler
  fun on(event: ProjectRenamedEvent, @SequenceNumber aggregateVersion: Long) =
      updateProjection(event.aggregateIdentifier) {
        it.name = event.newName
        it.version = aggregateVersion
      }

  @EventHandler
  fun on(event: ProjectRescheduledEvent, @SequenceNumber aggregateVersion: Long) =
      updateProjection(event.aggregateIdentifier) {
        it.plannedStartDate = event.newStartDate
        it.deadline = event.newDeadline
        it.version = aggregateVersion
      }

  @EventHandler
  fun on(event: TaskCreatedEvent) {
    lookupRepository.save(
        ProjectByTaskLookupProjection(taskId = event.identifier, projectId = event.projectId))
    updateProjection(event.projectId) {
      it.allTasksCount++
      it.plannedTasksCount++
    }
  }

  @EventHandler
  fun on(event: TaskStartedEvent) =
      lookupRepository.findById(event.identifier).ifPresent { task ->
        updateProjection(task.projectId) {
          it.plannedTasksCount--
          it.startedTasksCount++
        }
      }

  @EventHandler
  fun on(event: TaskCompletedEvent) =
      lookupRepository.findById(event.identifier).ifPresent { task ->
        updateProjection(task.projectId) {
          it.startedTasksCount--
          it.completedTasksCount++
        }
      }

  private fun updateProjection(
      identifier: ProjectId,
      stateChanges: (ProjectDetailsProjection) -> Unit
  ) {
    repository.findById(identifier).get().also {
      stateChanges.invoke(it)
      saveProjection(it)
    }
  }

  private fun saveProjection(projection: ProjectDetailsProjection) {
    repository.save(projection).also { savedProjection -> updateQuerySubscribers(savedProjection) }
  }

  private fun updateQuerySubscribers(project: ProjectDetailsProjection) {
    queryUpdateEmitter.emit<ProjectDetailsQuery, ProjectDetailsQueryResult>(
        project.toQueryResult()) { query -> query.projectId == project.identifier }
  }

  @ResetHandler
  fun reset() {
    repository.deleteAll()
    lookupRepository.deleteAll()
  }
}
