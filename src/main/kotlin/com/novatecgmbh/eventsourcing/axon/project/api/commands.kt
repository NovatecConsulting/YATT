package com.novatecgmbh.eventsourcing.axon.project.api

import java.time.LocalDate
import org.axonframework.modelling.command.TargetAggregateIdentifier
import org.axonframework.modelling.command.TargetAggregateVersion

data class CreateProjectCommand(
    @TargetAggregateIdentifier val projectId: String,
    val projectName: String,
    val plannedStartDate: LocalDate,
    val deadline: LocalDate,
)

data class UpdateProjectCommand(
    @TargetAggregateVersion val aggregateVersion: Long,
    @TargetAggregateIdentifier val projectId: String,
    val projectName: String,
    val plannedStartDate: LocalDate,
    val deadline: LocalDate,
)

data class RenameProjectCommand(
    @TargetAggregateVersion val aggregateVersion: Long,
    @TargetAggregateIdentifier
    val projectId: String,
    val newName: String,
)

data class RescheduleProjectCommand(
    @TargetAggregateVersion val aggregateVersion: Long,
    @TargetAggregateIdentifier
    val projectId: String,
    val newStartDate: LocalDate,
    val newDeadline: LocalDate,
)
