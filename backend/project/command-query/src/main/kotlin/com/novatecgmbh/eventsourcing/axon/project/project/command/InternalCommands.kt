package com.novatecgmbh.eventsourcing.axon.project.project.command

import com.novatecgmbh.eventsourcing.axon.project.project.api.ProjectCommand
import com.novatecgmbh.eventsourcing.axon.project.project.api.ProjectId
import java.time.LocalDate
import org.axonframework.modelling.command.TargetAggregateIdentifier

data class UpdateActualScheduleInternalCommand(
    @TargetAggregateIdentifier override val aggregateIdentifier: ProjectId,
    val startDate: LocalDate,
    val endDate: LocalDate,
) : ProjectCommand(aggregateIdentifier)
