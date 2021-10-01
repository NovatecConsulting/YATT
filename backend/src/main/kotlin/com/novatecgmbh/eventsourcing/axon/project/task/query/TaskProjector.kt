package com.novatecgmbh.eventsourcing.axon.project.task.query

import com.novatecgmbh.eventsourcing.axon.project.task.api.*
import com.novatecgmbh.eventsourcing.axon.project.task.api.TaskStatusEnum.*
import java.util.*
import org.axonframework.config.ProcessingGroup
import org.axonframework.eventhandling.EventHandler
import org.axonframework.eventhandling.ResetHandler
import org.axonframework.eventhandling.SequenceNumber
import org.axonframework.extensions.kotlin.emit
import org.axonframework.queryhandling.QueryHandler
import org.axonframework.queryhandling.QueryUpdateEmitter
import org.springframework.stereotype.Component

@Component
@ProcessingGroup("task-projector")
class TaskProjector(
    private val repository: TaskProjectionRepository,
    private val queryUpdateEmitter: QueryUpdateEmitter
) {
  @EventHandler
  fun on(event: TaskCreatedEvent, @SequenceNumber aggregateVersion: Long) {
    saveProjection(
        TaskProjection(
            identifier = event.identifier,
            version = aggregateVersion,
            projectId = event.projectId,
            name = event.name,
            description = event.description,
            startDate = event.startDate,
            endDate = event.endDate,
            status = PLANNED))
  }

  @EventHandler
  fun on(event: TaskDescriptionUpdatedEvent, @SequenceNumber aggregateVersion: Long) {
    updateProjection(event.identifier) {
      it.name = event.name
      it.description = event.description
    }
  }

  @EventHandler
  fun on(event: TaskRescheduledEvent, @SequenceNumber aggregateVersion: Long) {
    updateProjection(event.identifier) {
      it.startDate = event.startDate
      it.endDate = event.endDate
    }
  }

  @EventHandler
  fun on(event: TaskStartedEvent, @SequenceNumber aggregateVersion: Long) {
    updateProjection(event.identifier) { it.status = STARTED }
  }

  @EventHandler
  fun on(event: TaskCompletedEvent, @SequenceNumber aggregateVersion: Long) {
    updateProjection(event.identifier) { it.status = COMPLETED }
  }

  private fun updateProjection(identifier: TaskId, stateChanges: (TaskProjection) -> Unit) {
    repository.findById(identifier).get().also {
      stateChanges.invoke(it)
      saveProjection(it)
    }
  }

  private fun saveProjection(projection: TaskProjection) {
    repository.save(projection).also { savedProjection -> updateQuerySubscribers(savedProjection) }
  }

  private fun updateQuerySubscribers(task: TaskProjection) {
    queryUpdateEmitter.emit<TaskQuery, TaskQueryResult>(task.toQueryResult()) { query ->
      query.taskId == task.identifier
    }

    queryUpdateEmitter.emit<TasksByProjectQuery, TaskQueryResult>(task.toQueryResult()) { query ->
      query.projectId == task.projectId
    }
  }

  @ResetHandler fun reset() = repository.deleteAll()

  @QueryHandler
  fun handle(query: TasksByProjectQuery): Iterable<TaskQueryResult> =
      repository.findAllByProjectId(query.projectId).map { it.toQueryResult() }

  @QueryHandler
  fun handle(query: TaskQuery): Optional<TaskQueryResult> =
      repository.findById(query.taskId).map { it.toQueryResult() }
}
