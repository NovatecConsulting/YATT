package com.novatecgmbh.eventsourcing.axon.project.project.command.eventhandler

import com.novatecgmbh.eventsourcing.axon.application.sequencing.SequenceIdentifier
import com.novatecgmbh.eventsourcing.axon.project.project.api.ProjectId
import com.novatecgmbh.eventsourcing.axon.project.project.command.UpdateActualScheduleInternalCommand
import com.novatecgmbh.eventsourcing.axon.project.task.api.TaskCreatedEvent
import com.novatecgmbh.eventsourcing.axon.project.task.api.TaskId
import com.novatecgmbh.eventsourcing.axon.project.task.api.TaskRescheduledEvent
import java.time.LocalDate
import javax.persistence.*
import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.config.ProcessingGroup
import org.axonframework.eventhandling.DisallowReplay
import org.axonframework.eventhandling.EventHandler
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Component

// TODO if failed handling -> retry
@Component
@ProcessingGroup("actual-project-schedule-calculator")
class ActualProjectScheduleCalculator(
    val commandGateway: CommandGateway,
    val repository: ProjectTaskScheduleProjectionRepository
) {

  @EventHandler
  @DisallowReplay
  fun on(
      event: TaskCreatedEvent,
  ) {
    saveProjection(
        TaskScheduleProjection(
            taskId = event.identifier,
            projectId = event.projectId,
            startDate = event.startDate,
            endDate = event.endDate))
        .also {
          repository.findActualStartAndEndDate(event.projectId).let {
            commandGateway.send<Unit>(
                UpdateActualScheduleInternalCommand(event.projectId, it.startDate, it.endDate))
          }
        }
  }

  @EventHandler
  @DisallowReplay
  fun on(
      event: TaskRescheduledEvent,
      @SequenceIdentifier sequenceIdentifier: String,
  ) {
    updateProjection(event.identifier) {
      it.startDate = event.startDate
      it.endDate = event.endDate
    }
        .also {
          repository.findActualStartAndEndDate(ProjectId(sequenceIdentifier)).let {
            commandGateway.send<Unit>(
                UpdateActualScheduleInternalCommand(
                    ProjectId(sequenceIdentifier), it.startDate, it.endDate))
          }
        }
  }

  private fun updateProjection(identifier: TaskId, stateChanges: (TaskScheduleProjection) -> Unit) {
    repository.findById(identifier).get().also {
      stateChanges.invoke(it)
      saveProjection(it)
    }
  }

  private fun saveProjection(projection: TaskScheduleProjection) {
    repository.save(projection)
  }
}

interface ProjectTaskScheduleProjectionRepository : JpaRepository<TaskScheduleProjection, TaskId> {
  @Query(
      "select new com.novatecgmbh.eventsourcing.axon.project.project.command.eventhandler.ActualSchedule(min(t.startDate), max(t.endDate))" +
          "from TaskScheduleProjection t " +
          "where t.projectId = :projectId")
  fun findActualStartAndEndDate(projectId: ProjectId): ActualSchedule
}

data class ActualSchedule(val startDate: LocalDate, val endDate: LocalDate)

@Entity
@Table(name = "task_schedule_projection")
class TaskScheduleProjection(
    @EmbeddedId var taskId: TaskId,
    @Embedded
    @AttributeOverrides(
        AttributeOverride(
            name = "identifier", column = Column(name = "projectId", nullable = false)),
    )
    var projectId: ProjectId,
    @Column(nullable = false) var startDate: LocalDate,
    @Column(nullable = false) var endDate: LocalDate,
)
