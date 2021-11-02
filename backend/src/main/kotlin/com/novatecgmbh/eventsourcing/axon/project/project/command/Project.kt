package com.novatecgmbh.eventsourcing.axon.project.project.command

import com.novatecgmbh.eventsourcing.axon.common.command.AlreadyExistsException
import com.novatecgmbh.eventsourcing.axon.common.command.BaseAggregate
import com.novatecgmbh.eventsourcing.axon.company.company.api.CompanyId
import com.novatecgmbh.eventsourcing.axon.project.project.api.*
import com.novatecgmbh.eventsourcing.axon.project.project.api.ProjectStatus.DELAYED
import com.novatecgmbh.eventsourcing.axon.project.project.api.ProjectStatus.ON_TIME
import java.time.LocalDate
import org.axonframework.commandhandling.CommandHandler
import org.axonframework.eventsourcing.EventSourcingHandler
import org.axonframework.eventsourcing.conflictresolution.ConflictResolver
import org.axonframework.modelling.command.AggregateCreationPolicy
import org.axonframework.modelling.command.AggregateIdentifier
import org.axonframework.modelling.command.AggregateLifecycle
import org.axonframework.modelling.command.CreationPolicy
import org.axonframework.spring.stereotype.Aggregate

@Aggregate
class Project : BaseAggregate() {
  @AggregateIdentifier private lateinit var aggregateIdentifier: ProjectId
  private lateinit var projectName: String
  private lateinit var plannedStartDate: LocalDate
  private lateinit var deadline: LocalDate
  private lateinit var companyId: CompanyId
  private lateinit var status: ProjectStatus
  private var actualEndDate: LocalDate? = null

  @CommandHandler
  @CreationPolicy(AggregateCreationPolicy.CREATE_IF_MISSING)
  fun handle(command: CreateProjectCommand): ProjectId {
    if (::aggregateIdentifier.isInitialized) {
      throw AlreadyExistsException()
    }
    if (command.plannedStartDate.isAfter(command.deadline)) {
      throw IllegalArgumentException("Start date can't be after deadline")
    }
    apply(
        ProjectCreatedEvent(
            aggregateIdentifier = command.aggregateIdentifier,
            projectName = command.projectName,
            plannedStartDate = command.plannedStartDate,
            deadline = command.deadline,
            companyId = command.companyId,
            status = ON_TIME),
        sequenceIdentifier = command.aggregateIdentifier.identifier)
    return command.aggregateIdentifier
  }

  @CommandHandler
  fun handle(command: RenameProjectCommand, conflictResolver: ConflictResolver): Long {
    conflictResolver.detectConflicts {
      val anyRelevantEventsInPastOccured =
          it.stream().anyMatch { event -> event.payload is ProjectRenamedEvent }
      anyRelevantEventsInPastOccured && command.newName != projectName
    }
    if (command.newName != projectName) {
      apply(
          ProjectRenamedEvent(
              aggregateIdentifier = command.aggregateIdentifier,
              newName = command.newName,
          ))
    }
    return AggregateLifecycle.getVersion()
  }

  @CommandHandler
  fun handle(command: RescheduleProjectCommand, conflictResolver: ConflictResolver): Long {
    conflictResolver.detectConflicts {
      val anyRelevantEventsInPastOccured =
          it.stream().anyMatch { event -> event.payload is ProjectRescheduledEvent }
      anyRelevantEventsInPastOccured &&
          (command.newStartDate != plannedStartDate || command.newDeadline != deadline)
    }
    if (command.newStartDate.isAfter(command.newDeadline)) {
      throw IllegalArgumentException("Start date can't be after deadline")
    } else {
      if (command.newStartDate != plannedStartDate || command.newDeadline != deadline) {
        apply(
            ProjectRescheduledEvent(
                aggregateIdentifier = command.aggregateIdentifier,
                newStartDate = command.newStartDate,
                newDeadline = command.newDeadline,
            ))

        actualEndDate?.let { actualEndDate ->
          val isProjectDelayed = actualEndDate.isAfter(deadline)
          if (isProjectDelayed && status != DELAYED) {
            apply(ProjectDelayedEvent(command.aggregateIdentifier, actualEndDate))
          } else if (!isProjectDelayed && status != ON_TIME) {
            apply(ProjectOnTimeEvent(command.aggregateIdentifier, actualEndDate))
          }
        }
      }
      return AggregateLifecycle.getVersion()
    }
  }

  @CommandHandler
  fun handle(command: UpdateProjectCommand, conflictResolver: ConflictResolver): Long {
    handle(
        RenameProjectCommand(
            command.aggregateIdentifier, command.aggregateVersion, command.projectName),
        conflictResolver,
    )
    handle(
        RescheduleProjectCommand(
            command.aggregateIdentifier,
            command.aggregateVersion,
            command.plannedStartDate,
            command.deadline,
        ),
        conflictResolver,
    )
    return AggregateLifecycle.getVersion()
  }

  @EventSourcingHandler
  fun on(event: ProjectCreatedEvent) {
    aggregateIdentifier = event.aggregateIdentifier
    projectName = event.projectName
    plannedStartDate = event.plannedStartDate
    deadline = event.deadline
    companyId = event.companyId
    status = ON_TIME
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

  @CommandHandler
  fun handle(command: UpdateActualScheduleInternalCommand) {
    val isProjectDelayed = command.endDate.isAfter(deadline)
    val hasActualEndDateChanged = command.endDate != actualEndDate
    if (isProjectDelayed && (status != DELAYED || hasActualEndDateChanged)) {
      apply(ProjectDelayedEvent(command.aggregateIdentifier, command.endDate))
    } else if (!isProjectDelayed && (status != ON_TIME || hasActualEndDateChanged)) {
      apply(ProjectOnTimeEvent(command.aggregateIdentifier, command.endDate))
    }
  }

  @EventSourcingHandler
  fun on(event: ProjectDelayedEvent) {
    status = DELAYED
    actualEndDate = event.actualEndDate
  }

  @EventSourcingHandler
  fun on(event: ProjectOnTimeEvent) {
    status = ON_TIME
    actualEndDate = event.actualEndDate
  }

  override fun getSequenceIdentifier() = aggregateIdentifier.identifier
}
