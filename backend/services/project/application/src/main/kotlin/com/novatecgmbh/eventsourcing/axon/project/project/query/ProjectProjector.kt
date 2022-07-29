package com.novatecgmbh.eventsourcing.axon.project.project.query

import com.novatecgmbh.eventsourcing.axon.common.api.AggregateReference
import com.novatecgmbh.eventsourcing.axon.company.company.api.CompanyQuery
import com.novatecgmbh.eventsourcing.axon.company.company.api.CompanyQueryResult
import com.novatecgmbh.eventsourcing.axon.project.authorization.acl.ProjectAclRepository
import com.novatecgmbh.eventsourcing.axon.project.project.api.*
import com.novatecgmbh.eventsourcing.axon.project.project.api.ProjectStatus.DELAYED
import com.novatecgmbh.eventsourcing.axon.project.project.api.ProjectStatus.ON_TIME
import com.novatecgmbh.eventsourcing.axon.project.task.api.TaskCompletedEvent
import com.novatecgmbh.eventsourcing.axon.project.task.api.TaskCreatedEvent
import com.novatecgmbh.eventsourcing.axon.project.task.api.TaskId
import com.novatecgmbh.eventsourcing.axon.project.task.api.TaskStartedEvent
import javax.persistence.*
import org.axonframework.config.ProcessingGroup
import org.axonframework.eventhandling.EventHandler
import org.axonframework.eventhandling.ResetHandler
import org.axonframework.eventhandling.SequenceNumber
import org.axonframework.extensions.kotlin.emit
import org.axonframework.extensions.kotlin.queryOptional
import org.axonframework.queryhandling.QueryGateway
import org.axonframework.queryhandling.QueryUpdateEmitter
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Component
import org.springframework.stereotype.Repository

@Component
@ProcessingGroup("project-projector")
class ProjectProjector(
    private val repository: ProjectProjectionRepository,
    private val lookupRepository: ProjectByTaskLookupProjectionRepository,
    private val aclRepository: ProjectAclRepository,
    private val queryUpdateEmitter: QueryUpdateEmitter,
    private val queryGateway: QueryGateway
) {
  @EventHandler
  fun on(event: ProjectCreatedEvent, @SequenceNumber aggregateVersion: Long) {
    val company =
        queryGateway
            .queryOptional<CompanyQueryResult, CompanyQuery>(CompanyQuery(event.companyId))
            .get()
    saveProjection(
        ProjectProjection(
            identifier = event.aggregateIdentifier,
            version = aggregateVersion,
            name = event.projectName,
            plannedStartDate = event.plannedStartDate,
            deadline = event.deadline,
            companyReference =
                company
                    .map { it.toAggregateReference() }
                    .orElse(AggregateReference(event.companyId)),
            status = event.status,
            allTasksCount = 0,
            plannedTasksCount = 0,
            startedTasksCount = 0,
            completedTasksCount = 0,
        ))
  }

  @EventHandler
  fun on(event: ActualEndDateChangedEvent, @SequenceNumber aggregateVersion: Long) =
      updateProjection(event.aggregateIdentifier) {
        it.actualEndDate = event.actualEndDate
        it.version = aggregateVersion
      }

  @EventHandler
  fun on(event: ProjectDelayedEvent, @SequenceNumber aggregateVersion: Long) =
      updateProjection(event.aggregateIdentifier) {
        it.status = DELAYED
        it.version = aggregateVersion
      }

  @EventHandler
  fun on(event: ProjectOnTimeEvent, @SequenceNumber aggregateVersion: Long) =
      updateProjection(event.aggregateIdentifier) {
        it.status = ON_TIME
        it.version = aggregateVersion
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

  private fun updateProjection(identifier: ProjectId, stateChanges: (ProjectProjection) -> Unit) {
    repository.findById(identifier).get().also {
      stateChanges.invoke(it)
      saveProjection(it)
    }
  }

  private fun saveProjection(projection: ProjectProjection) {
    repository.save(projection).also { savedProjection -> updateQuerySubscribers(savedProjection) }
  }

  private fun updateQuerySubscribers(project: ProjectProjection) {
    queryUpdateEmitter.emit<ProjectQuery, ProjectQueryResult>(project.toQueryResult()) { query ->
      query.projectId == project.identifier
    }

    queryUpdateEmitter.emit<ProjectDetailsQuery, ProjectQueryResult>(project.toQueryResult()) {
        query ->
      query.projectId == project.identifier
    }

    queryUpdateEmitter.emit<MyProjectsQuery, ProjectQueryResult>(project.toQueryResult()) { query ->
      aclRepository
          .findAllUserWithAccessToProject(project.identifier.identifier)
          .contains(query.userId)
    }
  }

  @ResetHandler
  fun reset() {
    repository.deleteAll()
    lookupRepository.deleteAll()
  }
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
