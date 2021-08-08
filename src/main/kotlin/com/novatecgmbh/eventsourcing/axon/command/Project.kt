package com.novatecgmbh.eventsourcing.axon.command

import com.novatecgmbh.eventsourcing.axon.coreapi.*
import java.lang.IllegalArgumentException
import java.time.LocalDate
import org.axonframework.commandhandling.CommandHandler
import org.axonframework.eventsourcing.EventSourcingHandler
import org.axonframework.modelling.command.AggregateIdentifier
import org.axonframework.modelling.command.AggregateLifecycle
import org.axonframework.spring.stereotype.Aggregate

@Aggregate
class Project {
  @AggregateIdentifier private lateinit var projectId: String
  private lateinit var projectName: String
  private lateinit var plannedStartDate: LocalDate
  private lateinit var deadline: LocalDate

  constructor() // required by axon

  @CommandHandler
  constructor(command: CreateProjectCommand) {
    if (command.plannedStartDate.isAfter(command.deadline)) {
      throw IllegalArgumentException("Start date can't be after deadline")
    } else {
      AggregateLifecycle.apply(
          ProjectCreatedEvent(
              projectId = command.projectId,
              projectName = command.projectName,
              plannedStartDate = command.plannedStartDate,
              deadline = command.deadline,
          ))
    }
  }

  @CommandHandler
  fun handle(command: RenameProjectCommand) {
    AggregateLifecycle.apply(
        ProjectRenamedEvent(
            projectId = command.projectId,
            newName = command.newName,
        ))
  }

  @CommandHandler
  fun handle(command: RescheduleProjectCommand) {
    if (command.newStartDate.isAfter(command.newDeadline)) {
      throw IllegalArgumentException("Start date can't be after deadline")
    } else {
      AggregateLifecycle.apply(
          ProjectRescheduledEvent(
              projectId = command.projectId,
              newStartDate = command.newStartDate,
              newDeadline = command.newDeadline,
          ))
    }
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
