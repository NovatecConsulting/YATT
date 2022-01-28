package com.novatecgmbh.eventsourcing.axon.project.project.api

import com.novatecgmbh.eventsourcing.axon.common.api.AggregateReference
import com.novatecgmbh.eventsourcing.axon.company.company.api.CompanyId
import com.novatecgmbh.eventsourcing.axon.user.api.UserId
import java.time.LocalDate

class MyProjectsQuery(val userId: UserId)

data class ProjectQuery(val projectId: ProjectId)

data class ProjectQueryResult(
    val identifier: ProjectId,
    val version: Long,
    val name: String,
    val startDate: LocalDate,
    val deadline: LocalDate,
    val companyReference: AggregateReference<CompanyId>,
    val status: ProjectStatus,
    var actualEndDate: LocalDate? = null
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
