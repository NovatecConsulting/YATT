package com.novatecgmbh.eventsourcing.axon.project.query

import com.novatecgmbh.eventsourcing.axon.project.api.*
import org.axonframework.eventhandling.EventHandler
import org.axonframework.eventhandling.SequenceNumber
import org.axonframework.extensions.kotlin.emit
import org.axonframework.queryhandling.QueryHandler
import org.axonframework.queryhandling.QueryUpdateEmitter
import org.springframework.stereotype.Component

@Component
class ProjectProjection(
    private val repository: ProjectEntityRepository,
    private val queryUpdateEmitter: QueryUpdateEmitter
) {
  @EventHandler
  fun on(event: ProjectCreatedEvent, @SequenceNumber aggregateVersion: Long) {
    val savedEntity =
        repository.save(
            ProjectEntity(
                projectId = event.projectId,
                aggregateVersion = aggregateVersion,
                projectName = event.projectName,
                plannedStartDate = event.plannedStartDate,
                deadline = event.deadline,
            )
        )

    queryUpdateEmitter.emit<ProjectQuery, ProjectEntity>(savedEntity) { query ->
      query.projectId == event.projectId
    }
  }

  @EventHandler
  fun on(event: ProjectRenamedEvent, @SequenceNumber aggregateVersion: Long) =
      repository.findById(event.projectId).ifPresent {
        it.projectName = event.newName
        it.aggregateVersion = aggregateVersion

        queryUpdateEmitter.emit<ProjectQuery, ProjectEntity>(it) { query ->
          query.projectId == event.projectId
        }
      }

  @EventHandler
  fun on(event: ProjectRescheduledEvent, @SequenceNumber aggregateVersion: Long) =
      repository.findById(event.projectId).ifPresent {
        it.plannedStartDate = event.newStartDate
        it.deadline = event.newDeadline
        it.aggregateVersion = aggregateVersion

        queryUpdateEmitter.emit<ProjectQuery, ProjectEntity>(it) { query ->
          query.projectId == event.projectId
        }
      }

  @QueryHandler fun handle(query: ProjectQuery) = repository.findById(query.projectId)

  @QueryHandler
  fun handle(query: AllProjectsQuery): MutableList<ProjectEntity> = repository.findAll()
}
