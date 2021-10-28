package com.novatecgmbh.eventsourcing.axon.project.project.command.eventhandler

import com.novatecgmbh.eventsourcing.axon.application.auditing.AuditUserId
import com.novatecgmbh.eventsourcing.axon.application.auditing.SecurityContextHelper
import com.novatecgmbh.eventsourcing.axon.application.config.SequenceIdentifier
import com.novatecgmbh.eventsourcing.axon.project.project.api.ProjectId
import com.novatecgmbh.eventsourcing.axon.project.project.command.CheckTaskTimelinessInternalCommand
import com.novatecgmbh.eventsourcing.axon.project.task.api.TaskCreatedEvent
import com.novatecgmbh.eventsourcing.axon.project.task.api.TaskRescheduledEvent
import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.config.ProcessingGroup
import org.axonframework.eventhandling.DisallowReplay
import org.axonframework.eventhandling.EventHandler
import org.axonframework.messaging.unitofwork.UnitOfWork
import org.springframework.stereotype.Component

@Component
@ProcessingGroup("task-timeliness-checker")
class TaskTimelinessChecker(val commandGateway: CommandGateway) {

  @EventHandler
  @DisallowReplay
  fun on(
      event: TaskCreatedEvent,
      @SequenceIdentifier sequenceIdentifier: String,
      @AuditUserId userId: String,
      uow: UnitOfWork<*>
  ) {
    SecurityContextHelper.setAuthentication(userId)
    commandGateway.send<Unit>(
        CheckTaskTimelinessInternalCommand(
            ProjectId(sequenceIdentifier), event.identifier, event.startDate, event.endDate))
  }

  @EventHandler
  @DisallowReplay
  fun on(
      event: TaskRescheduledEvent,
      @SequenceIdentifier sequenceIdentifier: String,
      @AuditUserId userId: String,
  ) {
    SecurityContextHelper.setAuthentication(userId)
    commandGateway.send<Unit>(
        CheckTaskTimelinessInternalCommand(
            ProjectId(sequenceIdentifier), event.identifier, event.startDate, event.endDate))
  }
}
