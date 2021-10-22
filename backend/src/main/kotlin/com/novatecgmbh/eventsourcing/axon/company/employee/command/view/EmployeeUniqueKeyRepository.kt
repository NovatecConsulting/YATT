package com.novatecgmbh.eventsourcing.axon.company.employee.command.view

import com.novatecgmbh.eventsourcing.axon.company.company.api.CompanyId
import com.novatecgmbh.eventsourcing.axon.company.employee.api.EmployeeId
import com.novatecgmbh.eventsourcing.axon.user.api.UserId
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface EmployeeUniqueKeyRepository : JpaRepository<EmployeeUniqueKeyProjection, EmployeeId> {
  fun existsByCompanyIdAndUserId(companyId: CompanyId, userId: UserId): Boolean
}
