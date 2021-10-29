package com.novatecgmbh.eventsourcing.axon.project.project.command

import com.novatecgmbh.eventsourcing.axon.project.project.api.ProjectCommand
import com.novatecgmbh.eventsourcing.axon.project.project.api.ProjectId
import com.novatecgmbh.eventsourcing.axon.project.task.api.TaskId
import java.time.LocalDate
import org.axonframework.modelling.command.TargetAggregateIdentifier

data class RegisterTaskScheduleInternalCommand(
    @TargetAggregateIdentifier override val aggregateIdentifier: ProjectId,
    val taskId: TaskId,
    val startDate: LocalDate,
    val endDate: LocalDate,
) : ProjectCommand(aggregateIdentifier)
