package com.novatecgmbh.eventsourcing.axon.project.project.api

import com.novatecgmbh.eventsourcing.axon.project.project.command.ProjectId
import java.time.LocalDate
import org.axonframework.modelling.command.TargetAggregateIdentifier
import org.axonframework.modelling.command.TargetAggregateVersion

abstract class ProjectCommand(
    @TargetAggregateIdentifier open val aggregateIdentifier: ProjectId,
)

data class CreateProjectCommand(
    @TargetAggregateIdentifier override val aggregateIdentifier: ProjectId,
    val projectName: String,
    val plannedStartDate: LocalDate,
    val deadline: LocalDate,
) : ProjectCommand(aggregateIdentifier)

data class UpdateProjectCommand(
    @TargetAggregateIdentifier override val aggregateIdentifier: ProjectId,
    @TargetAggregateVersion val aggregateVersion: Long,
    val projectName: String,
    val plannedStartDate: LocalDate,
    val deadline: LocalDate,
) : ProjectCommand(aggregateIdentifier)

data class RenameProjectCommand(
    @TargetAggregateIdentifier override val aggregateIdentifier: ProjectId,
    @TargetAggregateVersion val aggregateVersion: Long,
    val newName: String,
) : ProjectCommand(aggregateIdentifier)

data class RescheduleProjectCommand(
    @TargetAggregateIdentifier override val aggregateIdentifier: ProjectId,
    @TargetAggregateVersion val aggregateVersion: Long,
    val newStartDate: LocalDate,
    val newDeadline: LocalDate,
) : ProjectCommand(aggregateIdentifier)
