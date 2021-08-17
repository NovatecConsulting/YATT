package com.novatecgmbh.eventsourcing.axon.project.command

import com.novatecgmbh.eventsourcing.axon.common.command.AlreadyExistsException
import com.novatecgmbh.eventsourcing.axon.project.api.*
import java.time.LocalDate
import org.axonframework.commandhandling.CommandHandler
import org.axonframework.eventsourcing.EventSourcingHandler
import org.axonframework.eventsourcing.conflictresolution.ConflictResolver
import org.axonframework.modelling.command.*
import org.axonframework.spring.stereotype.Aggregate

@Aggregate
class Project {
  @AggregateIdentifier private lateinit var projectId: String
  private lateinit var projectName: String
  private lateinit var plannedStartDate: LocalDate
  private lateinit var deadline: LocalDate

  @CommandHandler
  @CreationPolicy(AggregateCreationPolicy.CREATE_IF_MISSING)
  fun handle(command: CreateProjectCommand) {
    if (::projectId.isInitialized) {
      throw AlreadyExistsException()
    }
    if (command.plannedStartDate.isAfter(command.deadline)) {
      throw IllegalArgumentException("Start date can't be after deadline")
    }
    AggregateLifecycle.apply(
        ProjectCreatedEvent(
            projectId = command.projectId,
            projectName = command.projectName,
            plannedStartDate = command.plannedStartDate,
            deadline = command.deadline,
        ))
  }

  @CommandHandler
  fun handle(command: RenameProjectCommand, conflictResolver: ConflictResolver): Long {
    conflictResolver.detectConflicts {
      it.stream().anyMatch { event ->
        val hasDifferentAttributes =
            if (event.payload is ProjectRenamedEvent) {
              val eventPayload = event.payload as ProjectRenamedEvent
              eventPayload.newName != command.newName
            } else {
              false
            }
        event.payload is ProjectRenamedEvent && hasDifferentAttributes
      }
    }
    if (command.newName != projectName) {
      AggregateLifecycle.apply(
          ProjectRenamedEvent(
              projectId = command.projectId,
              newName = command.newName,
          ))
    }
    return AggregateLifecycle.getVersion()
  }

  @CommandHandler
  fun handle(command: RescheduleProjectCommand, conflictResolver: ConflictResolver): Long {
    conflictResolver.detectConflicts {
      it.stream().anyMatch { event ->
        val hasDifferentAttributes =
            if (event.payload is ProjectRescheduledEvent) {
              val eventPayload = event.payload as ProjectRescheduledEvent
              eventPayload.newStartDate != command.newStartDate ||
                  eventPayload.newDeadline != command.newDeadline
            } else {
              false
            }
        event.payload is ProjectRescheduledEvent && hasDifferentAttributes
      }
    }
    if (command.newStartDate.isAfter(command.newDeadline)) {
      throw IllegalArgumentException("Start date can't be after deadline")
    } else {
      if (command.newStartDate != plannedStartDate || command.newDeadline != deadline) {
        AggregateLifecycle.apply(
            ProjectRescheduledEvent(
                projectId = command.projectId,
                newStartDate = command.newStartDate,
                newDeadline = command.newDeadline,
            ))
      }
      return AggregateLifecycle.getVersion()
    }
  }

  @CommandHandler
  fun handle(command: UpdateProjectCommand, conflictResolver: ConflictResolver): Long {
    handle(
        RenameProjectCommand(command.aggregateVersion, command.projectId, command.projectName),
        conflictResolver,
    )
    handle(
        RescheduleProjectCommand(
            command.aggregateVersion,
            command.projectId,
            command.plannedStartDate,
            command.deadline,
        ),
        conflictResolver,
    )
    return AggregateLifecycle.getVersion()
  }

  @EventSourcingHandler
  fun on(event: ProjectCreatedEvent) {
    projectId = event.projectId
    projectName = event.projectName
    plannedStartDate = event.plannedStartDate
    deadline = event.deadline
  }

  @EventSourcingHandler
  fun on(event: ProjectRenamedEvent) {
    projectName = event.newName
  }

  @EventSourcingHandler
  fun on(event: ProjectRescheduledEvent) {
    plannedStartDate = event.newStartDate
    deadline = event.newDeadline
  }
}
