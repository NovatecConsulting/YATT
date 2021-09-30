package com.novatecgmbh.eventsourcing.axon.project.project.query

import com.novatecgmbh.eventsourcing.axon.project.project.api.ProjectId
import com.novatecgmbh.eventsourcing.axon.project.project.api.ProjectQueryResult
import java.time.LocalDate
import javax.persistence.Column
import javax.persistence.EmbeddedId
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "projects")
class ProjectProjection(
    @EmbeddedId var identifier: ProjectId,
    @Column(nullable = false) var version: Long,
    @Column(nullable = false) var name: String,
    @Column(nullable = false) var plannedStartDate: LocalDate,
    @Column(nullable = false) var deadline: LocalDate,
) {
  fun toQueryResult() = ProjectQueryResult(identifier, version, name, plannedStartDate, deadline)
}