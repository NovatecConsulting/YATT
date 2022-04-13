package com.novatecgmbh.eventsourcing.axon.project.project.rsocket.dto

import com.novatecgmbh.eventsourcing.axon.company.company.api.CompanyId
import com.novatecgmbh.eventsourcing.axon.project.project.api.CreateProjectCommand
import com.novatecgmbh.eventsourcing.axon.project.project.api.ProjectId
import java.time.LocalDate

data class CreateProjectDto(
    val identifier: ProjectId = ProjectId(),
    val name: String,
    val startDate: LocalDate,
    val deadline: LocalDate,
    val companyId: CompanyId
) {
  fun toCommand() = CreateProjectCommand(identifier, name, startDate, deadline, companyId)
}
