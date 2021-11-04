package com.novatecgmbh.eventsourcing.axon.project.project.query

import com.novatecgmbh.eventsourcing.axon.common.api.AggregateReference
import com.novatecgmbh.eventsourcing.axon.company.company.api.CompanyId
import com.novatecgmbh.eventsourcing.axon.project.project.api.ProjectId
import com.novatecgmbh.eventsourcing.axon.project.project.api.ProjectQueryResult
import com.novatecgmbh.eventsourcing.axon.project.project.api.ProjectStatus
import java.time.LocalDate
import javax.persistence.*
import javax.persistence.EnumType.STRING

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
            name = "identifier.identifier", column = Column(name = "companyId", nullable = false)),
        AttributeOverride(
            name = "displayName", column = Column(name = "companyName", nullable = true)))
    var companyReference: AggregateReference<CompanyId>,
    @Column(nullable = false) @Enumerated(STRING) var status: ProjectStatus,
    var actualEndDate: LocalDate? = null
) {
  fun toQueryResult() =
      ProjectQueryResult(
          identifier,
          version,
          name,
          plannedStartDate,
          deadline,
          companyReference,
          status,
          actualEndDate)
}
