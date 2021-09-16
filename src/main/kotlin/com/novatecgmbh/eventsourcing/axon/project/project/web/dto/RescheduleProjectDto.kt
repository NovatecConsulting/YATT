package com.novatecgmbh.eventsourcing.axon.project.project.web.dto

import java.time.LocalDate

data class RescheduleProjectDto(
    val aggregateVersion: Long,
    val newStartDate: LocalDate,
    val newDeadline: LocalDate
)
