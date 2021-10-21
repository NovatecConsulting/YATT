package com.novatecgmbh.eventsourcing.axon.project.project.query

import com.novatecgmbh.eventsourcing.axon.common.query.AggregateReference
import com.novatecgmbh.eventsourcing.axon.company.company.api.CompanyId
import com.novatecgmbh.eventsourcing.axon.project.project.api.ProjectId
import com.novatecgmbh.eventsourcing.axon.project.project.api.ProjectQueryResult
import java.time.LocalDate
import javax.persistence.*

@Entity
@Table(name = "projects")
class ProjectProjection(
    @EmbeddedId var identifier: ProjectId,
    @Column(nullable = false) var version: Long,
    @Column(nullable = false) var name: String,
    @Column(nullable = false) var plannedStartDate: LocalDate,
    @Column(nullable = false) var deadline: LocalDate,
    @Embedded
    @AttributeOverrides(
        AttributeOverride(
            name = "identifier.identifier",
            column = Column(name = "companyId", nullable = false)),
        AttributeOverride(
            name = "displayName",
            column = Column(name = "companyName", nullable = true)))
    var companyReference: AggregateReference<CompanyId>
) {
  fun toQueryResult() =
      ProjectQueryResult(identifier, version, name, plannedStartDate, deadline, companyReference)
}
