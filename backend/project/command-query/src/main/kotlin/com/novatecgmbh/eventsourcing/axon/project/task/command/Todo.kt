package com.novatecgmbh.eventsourcing.axon.project.task.command

import com.novatecgmbh.eventsourcing.axon.project.task.api.MarkTodoAsDoneCommand
import com.novatecgmbh.eventsourcing.axon.project.task.api.TodoId
import com.novatecgmbh.eventsourcing.axon.project.task.api.TodoMarkedAsDoneEvent
import org.axonframework.commandhandling.CommandHandler
import org.axonframework.eventsourcing.EventSourcingHandler
import org.axonframework.modelling.command.AggregateLifecycle
import org.axonframework.modelling.command.EntityId

class Todo(
    @EntityId(routingKey = "todoId") private val entityIdentifier: TodoId,
    private var description: String,
    isDone: Boolean
) {
  internal var isDone: Boolean = isDone
    private set

  @CommandHandler
  fun handle(command: MarkTodoAsDoneCommand) {
    if (!isDone) {
      // TODO rootContextId
      AggregateLifecycle.apply(TodoMarkedAsDoneEvent(command.identifier, command.todoId))
    }
  }

  @EventSourcingHandler
  fun on(event: TodoMarkedAsDoneEvent) {
    if (event.todoId == entityIdentifier) {
      isDone = true
    }
  }
}
