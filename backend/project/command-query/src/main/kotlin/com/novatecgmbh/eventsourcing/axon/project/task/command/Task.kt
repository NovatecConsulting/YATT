package com.novatecgmbh.eventsourcing.axon.project.task.command

import com.novatecgmbh.eventsourcing.axon.common.command.AlreadyExistsException
import com.novatecgmbh.eventsourcing.axon.common.command.BaseAggregate
import com.novatecgmbh.eventsourcing.axon.common.command.PreconditionFailedException
import com.novatecgmbh.eventsourcing.axon.project.project.api.ProjectId
import com.novatecgmbh.eventsourcing.axon.project.references.ReferenceCheckerService
import com.novatecgmbh.eventsourcing.axon.project.task.api.*
import com.novatecgmbh.eventsourcing.axon.project.task.api.TaskStatusEnum.*
import java.time.LocalDate
import org.axonframework.commandhandling.CommandHandler
import org.axonframework.eventsourcing.EventSourcingHandler
import org.axonframework.modelling.command.AggregateCreationPolicy.CREATE_IF_MISSING
import org.axonframework.modelling.command.AggregateIdentifier
import org.axonframework.modelling.command.AggregateMember
import org.axonframework.modelling.command.CreationPolicy
import org.axonframework.spring.stereotype.Aggregate
import org.springframework.beans.factory.annotation.Autowired

@Aggregate
class Task : BaseAggregate() {
  @AggregateIdentifier private lateinit var aggregateIdentifier: TaskId
  private lateinit var projectId: ProjectId
  private lateinit var name: String
  private var description: String? = null
  private lateinit var startDate: LocalDate
  private lateinit var endDate: LocalDate
  private lateinit var status: TaskStatusEnum
  @AggregateMember private var todos: MutableList<Todo> = mutableListOf()

  @CommandHandler
  @CreationPolicy(CREATE_IF_MISSING)
  fun handle(
      command: CreateTaskCommand,
      @Autowired referenceCheckerService: ReferenceCheckerService
  ): TaskId {
    assertAggregateDoesNotExistYet()
    referenceCheckerService.assertProjectExists(command.projectId.identifier)
    assertStartDateBeforeEndDate(command.startDate, command.endDate)
    apply(
        TaskCreatedEvent(
            identifier = command.identifier,
            projectId = command.projectId,
            name = command.name,
            description = command.description,
            startDate = command.startDate,
            endDate = command.endDate),
        rootContextId = command.projectId.identifier)
    return command.identifier
  }

  private fun assertAggregateDoesNotExistYet() {
    if (::aggregateIdentifier.isInitialized) {
      throw AlreadyExistsException()
    }
  }

  @CommandHandler
  fun handle(command: RenameTaskCommand): TaskId {
    if (status == COMPLETED) {
      throw IllegalArgumentException("Task is already completed. Name cannot be changed anymore.")
    }
    apply(TaskRenamedEvent(identifier = command.identifier, name = command.name))
    return aggregateIdentifier
  }

  @EventSourcingHandler
  fun on(event: TaskRenamedEvent) {
    name = event.name
  }

  @CommandHandler
  fun handle(command: ChangeTaskDescriptionCommand): TaskId {
    if (status == COMPLETED) {
      throw IllegalArgumentException(
          "Task is already completed. Description cannot be changed anymore.")
    }
    apply(
        TaskDescriptionUpdatedEvent(
            identifier = command.identifier, description = command.description))
    return aggregateIdentifier
  }

  @CommandHandler
  fun handle(command: RescheduleTaskCommand): TaskId {
    if (status == COMPLETED) {
      throw IllegalArgumentException(
          "Task is already completed and can not be rescheduled anymore.")
    }
    if (status != PLANNED && startDate != command.startDate) {
      throw IllegalArgumentException(
          "Task has already started. The start date can not be changed anymore")
    } else {
      apply(
          TaskRescheduledEvent(
              identifier = command.identifier,
              startDate = command.startDate,
              endDate = command.endDate))
    }
    return aggregateIdentifier
  }

  @CommandHandler
  fun handle(command: StartTaskCommand): TaskId {
    when (status) {
      PLANNED -> apply(TaskStartedEvent(aggregateIdentifier))
      STARTED -> {}
      COMPLETED -> throw IllegalStateException("Task is already completed.")
    }
    return aggregateIdentifier
  }

  @CommandHandler
  fun handle(command: CompleteTaskCommand): TaskId {
    when (status) {
      PLANNED -> throw IllegalStateException("Task has not yet been started.")
      STARTED -> {
        if (isAnyTodoNotDone()) {
          throw PreconditionFailedException("Can't complete task if not all todos are done.")
        }
        apply(TaskCompletedEvent(aggregateIdentifier))
      }
      COMPLETED -> {}
    }
    return aggregateIdentifier
  }

  private fun isAnyTodoNotDone() = todos.any { !it.isDone }

  private fun assertStartDateBeforeEndDate(startDate: LocalDate, endDate: LocalDate) {
    if (startDate.isAfter(endDate)) {
      throw IllegalArgumentException("Start date can't be after end date")
    }
  }

  @EventSourcingHandler
  fun on(event: TaskCreatedEvent) {
    aggregateIdentifier = event.identifier
    projectId = event.projectId
    name = event.name
    description = event.description
    startDate = event.startDate
    endDate = event.endDate
    status = PLANNED
  }

  @EventSourcingHandler
  fun on(event: TaskDescriptionUpdatedEvent) {
    description = event.description
  }

  @EventSourcingHandler
  fun on(event: TaskRescheduledEvent) {
    startDate = event.startDate
    endDate = event.endDate
  }

  @EventSourcingHandler
  fun on(event: TaskStartedEvent) {
    status = STARTED
  }

  @EventSourcingHandler
  fun on(event: TaskCompletedEvent) {
    status = COMPLETED
  }

  @CommandHandler
  fun handle(command: AddTodoCommand) {
    if (status == COMPLETED) {
      throw IllegalArgumentException("Task is already completed. Todo can not be added anymore.")
    }
    apply(TodoAddedEvent(command.identifier, command.todoId, command.description, isDone = false))
  }

  @EventSourcingHandler
  fun on(event: TodoAddedEvent) {
    todos.add(Todo(event.todoId, event.description, event.isDone))
  }

  @CommandHandler
  fun handle(command: RemoveTodoCommand) {
    if (status == COMPLETED) {
      throw IllegalArgumentException("Task is already completed. Todo can not be removed anymore.")
    }
    apply(TodoRemovedEvent(command.identifier, command.todoId))
  }

  @EventSourcingHandler
  fun on(event: TodoRemovedEvent) {
    todos.removeIf { it.entityIdentifier == event.todoId }
  }

  override fun getRootContextId() = projectId.identifier
}
