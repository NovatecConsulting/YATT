package com.novatecgmbh.eventsourcing.axon.project.project.command

import com.novatecgmbh.eventsourcing.axon.application.auditing.AuditUserId
import com.novatecgmbh.eventsourcing.axon.common.command.AlreadyExistsException
import com.novatecgmbh.eventsourcing.axon.common.command.BaseAggregate
import com.novatecgmbh.eventsourcing.axon.company.company.api.CompanyId
import com.novatecgmbh.eventsourcing.axon.project.participant.command.Participant
import com.novatecgmbh.eventsourcing.axon.project.project.api.*
import com.novatecgmbh.eventsourcing.axon.project.project.api.ProjectStatus.DELAYED
import com.novatecgmbh.eventsourcing.axon.project.project.api.ProjectStatus.ON_TIME
import com.novatecgmbh.eventsourcing.axon.user.api.UserId
import java.time.LocalDate
import org.axonframework.commandhandling.CommandHandler
import org.axonframework.eventsourcing.EventSourcingHandler
import org.axonframework.eventsourcing.conflictresolution.ConflictResolver
import org.axonframework.modelling.command.AggregateCreationPolicy.CREATE_IF_MISSING
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
  @CreationPolicy(CREATE_IF_MISSING)
  fun handle(command: CreateProjectCommand, @AuditUserId userId: String): ProjectId {
    assertAggregateDoesNotExistYet()
    assertStartDateBeforeDeadline(command.plannedStartDate, command.deadline)
    apply(
        ProjectCreatedEvent(
            aggregateIdentifier = command.aggregateIdentifier,
            projectName = command.projectName,
            plannedStartDate = command.plannedStartDate,
            deadline = command.deadline,
            companyId = command.companyId,
            status = ON_TIME),
        rootContextId = command.aggregateIdentifier.identifier)

    // automatically create the first participant for the user that created the project
    AggregateLifecycle.createNew(Participant::class.java) {
      Participant(command.aggregateIdentifier, UserId(userId), command.companyId)
    }
    return command.aggregateIdentifier
  }

  private fun assertAggregateDoesNotExistYet() {
    if (::aggregateIdentifier.isInitialized) {
      throw AlreadyExistsException()
    }
  }

  private fun assertStartDateBeforeDeadline(start: LocalDate, deadline: LocalDate) {
    if (!start.isBefore(deadline)) {
      throw IllegalArgumentException("Deadline must be after start date")
    }
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

    assertStartDateBeforeDeadline(command.newStartDate, command.newDeadline)
    if (projectWasRescheduled(command.newStartDate, command.newDeadline)) {
      apply(
          ProjectRescheduledEvent(
              aggregateIdentifier = command.aggregateIdentifier,
              newStartDate = command.newStartDate,
              newDeadline = command.newDeadline,
          ))
    }

    applyEventIfProjectStatusChanged()
    return AggregateLifecycle.getVersion()
  }

  private fun projectWasRescheduled(newStartDate: LocalDate, newDeadline: LocalDate) =
      newStartDate != plannedStartDate || newDeadline != deadline

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
    val hasActualEndDateChanged = command.endDate != actualEndDate
    if (hasActualEndDateChanged) {
      apply(ActualEndDateChangedEvent(command.aggregateIdentifier, command.endDate))
    }

    applyEventIfProjectStatusChanged()
  }

  private fun applyEventIfProjectStatusChanged() {
    when (status) {
      DELAYED ->
          if (!isProjectDelayed()) {
            apply(ProjectOnTimeEvent(aggregateIdentifier))
          }
      ON_TIME ->
          if (isProjectDelayed()) {
            apply(ProjectDelayedEvent(aggregateIdentifier))
          }
    }
  }

  private fun isProjectDelayed(): Boolean = actualEndDate?.isAfter(deadline) ?: false

  @EventSourcingHandler
  fun on(event: ActualEndDateChangedEvent) {
    actualEndDate = event.actualEndDate
  }

  @EventSourcingHandler
  fun on(event: ProjectDelayedEvent) {
    status = DELAYED
  }

  @EventSourcingHandler
  fun on(event: ProjectOnTimeEvent) {
    status = ON_TIME
  }

  override fun getRootContextId() = aggregateIdentifier.identifier
}
