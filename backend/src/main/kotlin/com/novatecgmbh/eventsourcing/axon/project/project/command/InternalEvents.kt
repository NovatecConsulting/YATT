package com.novatecgmbh.eventsourcing.axon.project.project.command

import com.novatecgmbh.eventsourcing.axon.project.project.api.ProjectEvent
import com.novatecgmbh.eventsourcing.axon.project.project.api.ProjectId
import com.novatecgmbh.eventsourcing.axon.project.task.api.TaskId
import java.time.LocalDate

data class TaskDelayedInternalEvent(
    override val aggregateIdentifier: ProjectId,
    val taskId: TaskId,
    val startDate: LocalDate,
    val endDate: LocalDate,
) : ProjectEvent(aggregateIdentifier)

data class TaskOnTimeInternalEvent(
    override val aggregateIdentifier: ProjectId,
    val taskId: TaskId,
    val startDate: LocalDate,
    val endDate: LocalDate,
) : ProjectEvent(aggregateIdentifier)
