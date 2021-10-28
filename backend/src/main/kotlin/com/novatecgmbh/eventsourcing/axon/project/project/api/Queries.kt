package com.novatecgmbh.eventsourcing.axon.project.project.api

import com.novatecgmbh.eventsourcing.axon.common.query.AggregateReference
import com.novatecgmbh.eventsourcing.axon.company.company.api.CompanyId
import java.time.LocalDate

class AllProjectsQuery

data class ProjectQuery(val projectId: ProjectId)

data class ProjectQueryResult(
    val identifier: ProjectId,
    val version: Long,
    val name: String,
    val startDate: LocalDate,
    val deadline: LocalDate,
    val companyReference: AggregateReference<CompanyId>,
    val status: ProjectStatus
)

data class ProjectDetailsQuery(val projectId: ProjectId)

data class ProjectDetailsQueryResult(
    val identifier: ProjectId,
    val version: Long,
    val name: String,
    val startDate: LocalDate,
    val deadline: LocalDate,
    val allTasksCount: Long,
    val plannedTasksCount: Long,
    val startedTasksCount: Long,
    val completedTasksCount: Long,
)
