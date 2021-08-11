package com.novatecgmbh.eventsourcing.axon.query

import com.novatecgmbh.eventsourcing.axon.coreapi.*
import org.axonframework.eventhandling.EventHandler
import org.axonframework.queryhandling.QueryHandler
import org.axonframework.queryhandling.QueryUpdateEmitter
import org.springframework.stereotype.Component

@Component
class ProjectProjection(
    private val repository: ProjectEntityRepository,
    private val queryUpdateEmitter: QueryUpdateEmitter
) {
  @EventHandler
  fun on(event: ProjectCreatedEvent) {
    val savedEntity =
        repository.save(
            ProjectEntity(
                projectId = event.projectId,
                projectName = event.projectName,
                plannedStartDate = event.plannedStartDate,
                deadline = event.deadline,
            ))

    queryUpdateEmitter.emit(
        ProjectQuery::class.java,
        { query -> query.projectId == event.projectId },
        savedEntity,
    )
  }

  @EventHandler
  fun on(event: ProjectRenamedEvent) =
      repository.findById(event.projectId).ifPresent {
        it.projectName = event.newName

        queryUpdateEmitter.emit(
            ProjectQuery::class.java,
            { query -> query.projectId == event.projectId },
            it,
        )
      }

  @EventHandler
  fun on(event: ProjectRescheduledEvent) =
      repository.findById(event.projectId).ifPresent {
        it.plannedStartDate = event.newStartDate
        it.deadline = event.newDeadline

        queryUpdateEmitter.emit(
            ProjectQuery::class.java,
            { query -> query.projectId == event.projectId },
            it,
        )
      }

  @QueryHandler fun handle(query: ProjectQuery) = repository.findById(query.projectId)

  @QueryHandler
  fun handle(query: AllProjectsQuery): MutableList<ProjectEntity> = repository.findAll()
}
