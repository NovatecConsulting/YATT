package com.novatecgmbh.eventsourcing.axon.coreapi

import java.time.LocalDate
import org.axonframework.modelling.command.TargetAggregateIdentifier

data class CreateProjectCommand(
    @TargetAggregateIdentifier val projectId: String,
    val projectName: String,
    val plannedStartDate: LocalDate,
    val deadline: LocalDate,
)

data class RenameProjectCommand(
    @TargetAggregateIdentifier
    val projectId: String,
    val newName: String,
)

data class RescheduleProjectCommand(
    @TargetAggregateIdentifier
    val projectId: String,
    val newStartDate: LocalDate,
    val newDeadline: LocalDate,
)
