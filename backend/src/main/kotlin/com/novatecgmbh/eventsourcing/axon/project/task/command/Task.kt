package com.novatecgmbh.eventsourcing.axon.project.task.command

import com.novatecgmbh.eventsourcing.axon.common.command.AlreadyExistsException
import com.novatecgmbh.eventsourcing.axon.project.project.api.ProjectId
import com.novatecgmbh.eventsourcing.axon.project.project.command.Project
import com.novatecgmbh.eventsourcing.axon.project.task.api.*
import com.novatecgmbh.eventsourcing.axon.project.task.api.TaskStatusEnum.*
import java.time.LocalDate
import org.axonframework.commandhandling.CommandHandler
import org.axonframework.eventsourcing.EventSourcingHandler
import org.axonframework.modelling.command.AggregateCreationPolicy.CREATE_IF_MISSING
import org.axonframework.modelling.command.AggregateIdentifier
import org.axonframework.modelling.command.AggregateLifecycle.apply
import org.axonframework.modelling.command.AggregateNotFoundException
import org.axonframework.modelling.command.CreationPolicy
import org.axonframework.modelling.command.Repository
import org.axonframework.spring.stereotype.Aggregate
import org.springframework.beans.factory.annotation.Autowired

@Aggregate
class Task {
  @AggregateIdentifier private lateinit var identifier: TaskId
  private lateinit var projectId: ProjectId
  private lateinit var name: String
  private var description: String? = null
  private lateinit var startDate: LocalDate
  private lateinit var endDate: LocalDate
  private lateinit var status: TaskStatusEnum

  @CommandHandler
  @CreationPolicy(CREATE_IF_MISSING)
  fun handle(
      command: CreateTaskCommand,
      @Autowired projectRepository: Repository<Project>
  ): TaskId {
    if (::projectId.isInitialized) {
      throw AlreadyExistsException()
    }
    assertProjectExists(projectRepository, command.projectId)
    assertStartDateBeforeEndDate(command.startDate, command.endDate)
    apply(
        TaskCreatedEvent(
            identifier = command.identifier,
            projectId = command.projectId,
            name = command.name,
            description = command.description,
            startDate = command.startDate,
            endDate = command.endDate))
    return command.identifier
  }

  @CommandHandler
  fun handle(command: RenameTaskCommand): TaskId {
    if (status == COMPLETED) {
      throw IllegalArgumentException("Task is already completed. Name cannot be changed anymore.")
    }
    apply(TaskRenamedEvent(identifier = command.identifier, name = command.name))
    return identifier
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
    return identifier
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
    return identifier
  }

  @CommandHandler
  fun handle(command: StartTaskCommand): TaskId {
    when (status) {
      PLANNED -> apply(TaskStartedEvent(identifier))
      STARTED -> {}
      COMPLETED -> throw IllegalStateException("Task is already completed.")
    }
    return identifier
  }

  @CommandHandler
  fun handle(command: CompleteTaskCommand): TaskId {
    when (status) {
      PLANNED -> throw IllegalStateException("Task has not yet been started.")
      STARTED -> apply(TaskCompletedEvent(identifier))
      COMPLETED -> {}
    }
    return identifier
  }

  private fun assertProjectExists(projectRepository: Repository<Project>, projectId: ProjectId) {
    try {
      projectRepository.load(projectId.identifier)
    } catch (ex: AggregateNotFoundException) {
      throw IllegalArgumentException("Referenced Project does not exist")
    }
  }

  private fun assertStartDateBeforeEndDate(startDate: LocalDate, endDate: LocalDate) {
    if (startDate.isAfter(endDate)) {
      throw IllegalArgumentException("Start date can't be after end date")
    }
  }

  @EventSourcingHandler
  fun on(event: TaskCreatedEvent) {
    identifier = event.identifier
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
}
