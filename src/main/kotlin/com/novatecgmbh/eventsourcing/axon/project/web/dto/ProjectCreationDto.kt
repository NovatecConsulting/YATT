package com.novatecgmbh.eventsourcing.axon.project.web.dto

import java.time.LocalDate

data class ProjectCreationDto(
    val projectName: String,
    val plannedStartDate: LocalDate,
    val deadline: LocalDate,
)
