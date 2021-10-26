package com.novatecgmbh.eventsourcing.axon.project.project.api

import com.novatecgmbh.eventsourcing.axon.company.company.api.CompanyId
import java.time.LocalDate

abstract class ProjectEvent(open val aggregateIdentifier: ProjectId)

data class ProjectCreatedEvent(
    override val aggregateIdentifier: ProjectId,
    val projectName: String,
    val plannedStartDate: LocalDate,
    val deadline: LocalDate,
    val companyId: CompanyId
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
