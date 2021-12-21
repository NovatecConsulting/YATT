package com.novatecgmbh.eventsourcing.axon.project.task.command

import com.novatecgmbh.eventsourcing.axon.project.task.api.MarkTodoAsDoneCommand
import com.novatecgmbh.eventsourcing.axon.project.task.api.TodoId
import com.novatecgmbh.eventsourcing.axon.project.task.api.TodoMarkedAsDoneEvent
import org.axonframework.commandhandling.CommandHandler
import org.axonframework.eventsourcing.EventSourcingHandler
import org.axonframework.messaging.MetaData
import org.axonframework.messaging.Scope
import org.axonframework.modelling.command.AggregateLifecycle
import org.axonframework.modelling.command.EntityId
import org.axonframework.modelling.command.inspection.AnnotatedAggregate

class Todo(entityIdentifier: TodoId, private var description: String, isDone: Boolean) {
  @EntityId(routingKey = "todoId")
  internal var entityIdentifier: TodoId = entityIdentifier
    private set

  internal var isDone: Boolean = isDone
    private set

  @CommandHandler
  fun handle(command: MarkTodoAsDoneCommand): Long {
    if (!isDone) {
      // TODO rootContextId
      Scope.getCurrentScope<AnnotatedAggregate<Task>>().execute {
        AggregateLifecycle.apply(
            TodoMarkedAsDoneEvent(command.identifier, command.todoId),
            MetaData(mapOf("rootContextId" to it.getRootContextId())))
      }
    }
    return AggregateLifecycle.getVersion()
  }

  @EventSourcingHandler
  fun on(event: TodoMarkedAsDoneEvent) {
    if (event.todoId == entityIdentifier) {
      isDone = true
    }
  }
}
