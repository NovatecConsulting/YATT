package com.novatecgmbh.eventsourcing.axon.company.employee.query

import com.novatecgmbh.eventsourcing.axon.company.company.api.CompanyId
import com.novatecgmbh.eventsourcing.axon.company.employee.api.EmployeeId
import com.novatecgmbh.eventsourcing.axon.project.project.api.ProjectId
import com.novatecgmbh.eventsourcing.axon.project.task.query.TaskProjection
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository interface EmployeeProjectionRepository : JpaRepository<EmployeeProjection, EmployeeId> {
    fun findAllByCompanyId(companyId: CompanyId): MutableIterable<EmployeeProjection>
}
