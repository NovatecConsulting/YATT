package com.novatecgmbh.eventsourcing.axon.project.project.web.dto

import java.time.LocalDate

data class CreateProjectDto(
    val name: String,
    val plannedStartDate: LocalDate,
    val deadline: LocalDate,
)
