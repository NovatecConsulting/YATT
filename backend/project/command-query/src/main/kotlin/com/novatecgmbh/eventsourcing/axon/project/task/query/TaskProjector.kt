package com.novatecgmbh.eventsourcing.axon.project.task.query

import com.novatecgmbh.eventsourcing.axon.project.task.api.*
import com.novatecgmbh.eventsourcing.axon.project.task.api.TaskStatusEnum.*
import org.axonframework.config.ProcessingGroup
import org.axonframework.eventhandling.EventHandler
import org.axonframework.eventhandling.ResetHandler
import org.axonframework.eventhandling.SequenceNumber
import org.axonframework.extensions.kotlin.emit
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
            status = PLANNED,
            todos = emptyList()))
  }

  @EventHandler
  fun on(event: TaskRenamedEvent, @SequenceNumber aggregateVersion: Long) {
    updateProjection(event.identifier) {
      it.name = event.name
      it.version = aggregateVersion
    }
  }

  @EventHandler
  fun on(event: TaskDescriptionUpdatedEvent, @SequenceNumber aggregateVersion: Long) {
    updateProjection(event.identifier) {
      it.description = event.description
      it.version = aggregateVersion
    }
  }

  @EventHandler
  fun on(event: TaskRescheduledEvent, @SequenceNumber aggregateVersion: Long) {
    updateProjection(event.identifier) {
      it.startDate = event.startDate
      it.endDate = event.endDate
      it.version = aggregateVersion
    }
  }

  @EventHandler
  fun on(event: TaskStartedEvent, @SequenceNumber aggregateVersion: Long) {
    updateProjection(event.identifier) {
      it.status = STARTED
      it.version = aggregateVersion
    }
  }

  @EventHandler
  fun on(event: TaskCompletedEvent, @SequenceNumber aggregateVersion: Long) {
    updateProjection(event.identifier) {
      it.status = COMPLETED
      it.version = aggregateVersion
    }
  }

  @EventHandler
  fun on(event: TodoAddedEvent, @SequenceNumber aggregateVersion: Long) {
    updateProjection(event.identifier) {
      it.todos = it.todos.plus(Todo(event.todoId, event.description, event.isDone))
      it.version = aggregateVersion
    }
  }

  @EventHandler
  fun on(event: TodoRemovedEvent, @SequenceNumber aggregateVersion: Long) {
    updateProjection(event.identifier) {
      it.todos = it.todos.filterNot { todo -> todo.todoId == event.todoId }
      it.version = aggregateVersion
    }
  }

  @EventHandler
  fun on(event: TodoMarkedAsDoneEvent, @SequenceNumber aggregateVersion: Long) {
    updateProjection(event.identifier) {
      it.todos =
          it.todos.map { todo ->
            if (todo.todoId == event.todoId) {
              todo.apply { isDone = true }
            } else {
              todo
            }
          }
      it.version = aggregateVersion
    }
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
}
