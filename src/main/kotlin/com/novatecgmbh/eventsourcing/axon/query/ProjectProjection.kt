package com.novatecgmbh.eventsourcing.axon.query

import com.novatecgmbh.eventsourcing.axon.coreapi.*
import org.axonframework.eventhandling.EventHandler
import org.axonframework.queryhandling.QueryHandler
import org.springframework.stereotype.Component

@Component
class ProjectProjection(private val repository: ProjectEntityRepository) {
  @EventHandler
  fun on(event: ProjectCreatedEvent): ProjectEntity =
      repository.save(
          ProjectEntity(
              projectId = event.projectId,
              projectName = event.projectName,
              plannedStartDate = event.plannedStartDate,
              deadline = event.deadline,
          ))

  @EventHandler
  fun on(event: ProjectRenamedEvent) =
      repository.findById(event.projectId).ifPresent { it.projectName = event.newName }

  @EventHandler
  fun on(event: ProjectRescheduledEvent) =
      repository.findById(event.projectId).ifPresent {
        it.plannedStartDate = event.newStartDate
        it.deadline = event.newDeadline
      }

  @QueryHandler fun handle(query: ProjectQuery) = repository.findById(query.projectId)

  @QueryHandler
  fun handle(query: AllProjectsQuery): MutableList<ProjectEntity> = repository.findAll()
}
