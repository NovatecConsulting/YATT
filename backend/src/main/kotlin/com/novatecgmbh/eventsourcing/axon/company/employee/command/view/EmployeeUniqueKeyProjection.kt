package com.novatecgmbh.eventsourcing.axon.company.employee.command.view

import com.novatecgmbh.eventsourcing.axon.company.company.api.CompanyId
import com.novatecgmbh.eventsourcing.axon.company.employee.api.EmployeeId
import com.novatecgmbh.eventsourcing.axon.user.api.UserId
import javax.persistence.*

@Entity
@Table(
    name = "employee_unique_key",
    uniqueConstraints = [UniqueConstraint(columnNames = ["companyId", "userId"])])
class EmployeeUniqueKeyProjection(
    @EmbeddedId var identifier: EmployeeId,
    @Embedded
    @AttributeOverride(name = "identifier", column = Column(name = "companyId", nullable = false))
    var companyId: CompanyId,
    @Embedded
    @AttributeOverride(name = "identifier", column = Column(name = "userId", nullable = false))
    var userId: UserId,
)
