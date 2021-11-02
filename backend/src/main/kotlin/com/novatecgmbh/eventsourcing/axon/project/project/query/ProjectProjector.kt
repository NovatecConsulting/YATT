package com.novatecgmbh.eventsourcing.axon.project.project.query

import com.novatecgmbh.eventsourcing.axon.common.query.AggregateReference
import com.novatecgmbh.eventsourcing.axon.company.company.api.CompanyQuery
import com.novatecgmbh.eventsourcing.axon.company.company.api.CompanyQueryResult
import com.novatecgmbh.eventsourcing.axon.project.project.api.*
import com.novatecgmbh.eventsourcing.axon.project.project.api.ProjectStatus.DELAYED
import com.novatecgmbh.eventsourcing.axon.project.project.api.ProjectStatus.ON_TIME
import java.util.*
import org.axonframework.config.ProcessingGroup
import org.axonframework.eventhandling.EventHandler
import org.axonframework.eventhandling.ResetHandler
import org.axonframework.eventhandling.SequenceNumber
import org.axonframework.extensions.kotlin.emit
import org.axonframework.extensions.kotlin.queryOptional
import org.axonframework.queryhandling.QueryGateway
import org.axonframework.queryhandling.QueryHandler
import org.axonframework.queryhandling.QueryUpdateEmitter
import org.springframework.stereotype.Component

@Component
@ProcessingGroup("project-projector")
class ProjectProjector(
    private val repository: ProjectProjectionRepository,
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
            status = event.status))
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

    queryUpdateEmitter.emit<AllProjectsQuery, ProjectQueryResult>(project.toQueryResult()) { true }
  }

  @ResetHandler fun reset() = repository.deleteAll()

  @QueryHandler
  fun handle(query: ProjectQuery): Optional<ProjectQueryResult> =
      repository.findById(query.projectId).map { it.toQueryResult() }

  @QueryHandler
  fun handle(query: AllProjectsQuery): Iterable<ProjectQueryResult> =
      repository.findAll().map { it.toQueryResult() }
}
