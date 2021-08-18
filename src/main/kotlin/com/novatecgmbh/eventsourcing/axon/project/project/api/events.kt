package com.novatecgmbh.eventsourcing.axon.project.project.api

import java.time.LocalDate

data class ProjectCreatedEvent(
    val projectId: String,
    val projectName: String,
    val plannedStartDate: LocalDate,
    val deadline: LocalDate,
)

data class ProjectRenamedEvent(
    val projectId: String,
    val newName: String,
)

data class ProjectRescheduledEvent(
    val projectId: String,
    val newStartDate: LocalDate,
    val newDeadline: LocalDate,
)
