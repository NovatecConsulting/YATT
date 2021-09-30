package com.novatecgmbh.eventsourcing.axon.project.project.api

import java.time.LocalDate

class AllProjectsQuery

data class ProjectQuery(val projectId: ProjectId)

data class ProjectQueryResult(
    val identifier: ProjectId,
    val version: Long,
    val name: String,
    val plannedStartDate: LocalDate,
    val deadline: LocalDate
)
