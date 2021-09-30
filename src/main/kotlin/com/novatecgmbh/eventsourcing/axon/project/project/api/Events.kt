package com.novatecgmbh.eventsourcing.axon.project.project.api

import java.time.LocalDate

abstract class ProjectEvent(open val aggregateIdentifier: ProjectId)

data class ProjectCreatedEvent(
    override val aggregateIdentifier: ProjectId,
    val projectName: String,
    val plannedStartDate: LocalDate,
    val deadline: LocalDate,
) : ProjectEvent(aggregateIdentifier)

data class ProjectRenamedEvent(
    override val aggregateIdentifier: ProjectId,
    val newName: String,
) : ProjectEvent(aggregateIdentifier)

data class ProjectRescheduledEvent(
    override val aggregateIdentifier: ProjectId,
    val newStartDate: LocalDate,
    val newDeadline: LocalDate,
) : ProjectEvent(aggregateIdentifier)
