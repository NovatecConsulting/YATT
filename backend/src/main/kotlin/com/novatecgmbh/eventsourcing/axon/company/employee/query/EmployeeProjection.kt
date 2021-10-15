package com.novatecgmbh.eventsourcing.axon.company.employee.query

import com.novatecgmbh.eventsourcing.axon.company.company.api.CompanyId
import com.novatecgmbh.eventsourcing.axon.company.employee.api.EmployeeId
import com.novatecgmbh.eventsourcing.axon.company.employee.api.EmployeeQueryResult
import com.novatecgmbh.eventsourcing.axon.user.api.UserId
import javax.persistence.*

@Entity
@Table(name = "employee")
class EmployeeProjection(
    @EmbeddedId var identifier: EmployeeId,
    @Column(nullable = false) var version: Long,
    @Embedded
    @AttributeOverride(name = "identifier", column = Column(name = "companyId", nullable = false))
    var companyId: CompanyId,
    @Embedded
    @AttributeOverride(name = "identifier", column = Column(name = "userId", nullable = false))
    var userId: UserId,
    var userFirstName: String? = null,
    var userLastName: String? = null,
    @Column(nullable = false) var isAdmin: Boolean = false,
    @Column(nullable = false) var isProjectManager: Boolean = false
) {
  fun toQueryResult() =
      EmployeeQueryResult(
          identifier,
          version,
          companyId,
          userId,
          userFirstName,
          userLastName,
          isAdmin,
          isProjectManager)
}
