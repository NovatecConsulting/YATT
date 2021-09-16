package com.novatecgmbh.eventsourcing.axon.project.project.query

import com.novatecgmbh.eventsourcing.axon.project.project.api.*
import com.novatecgmbh.eventsourcing.axon.project.project.command.ProjectId
import org.axonframework.config.ProcessingGroup
import org.axonframework.eventhandling.EventHandler
import org.axonframework.eventhandling.ResetHandler
import org.axonframework.eventhandling.SequenceNumber
import org.axonframework.extensions.kotlin.emit
import org.axonframework.queryhandling.QueryHandler
import org.axonframework.queryhandling.QueryUpdateEmitter
import org.springframework.stereotype.Component

@Component
@ProcessingGroup("project-projector")
class ProjectProjector(
    private val repository: ProjectProjectionRepository,
    private val queryUpdateEmitter: QueryUpdateEmitter
) {
  @EventHandler
  fun on(event: ProjectCreatedEvent, @SequenceNumber aggregateVersion: Long) {
    saveProjection(
        ProjectProjection(
            identifier = event.aggregateIdentifier,
            version = aggregateVersion,
            name = event.projectName,
            plannedStartDate = event.plannedStartDate,
            deadline = event.deadline,
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
    queryUpdateEmitter.emit<ProjectQuery, ProjectProjection>(project) { query ->
      query.projectId == project.identifier
    }

    queryUpdateEmitter.emit<AllProjectsQuery, ProjectProjection>(project) { true }
  }

  @ResetHandler fun reset() = repository.deleteAll()

  @QueryHandler fun handle(query: ProjectQuery) = repository.findById(query.projectId)

  @QueryHandler
  fun handle(query: AllProjectsQuery): MutableList<ProjectProjection> = repository.findAll()
}
