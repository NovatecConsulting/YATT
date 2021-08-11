package com.novatecgmbh.eventsourcing.axon.web.dto

import java.time.LocalDate

data class ProjectCreateUpdateDto(
    val projectName: String,
    val plannedStartDate: LocalDate,
    val deadline: LocalDate,
)
