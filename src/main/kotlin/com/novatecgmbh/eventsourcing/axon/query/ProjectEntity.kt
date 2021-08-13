package com.novatecgmbh.eventsourcing.axon.query

import java.time.LocalDate
import javax.persistence.Entity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "projects")
class ProjectEntity(
    @Id var projectId: String,
    var aggregateVersion: Long,
    var projectName: String,
    var plannedStartDate: LocalDate,
    var deadline: LocalDate,
)

@Repository
interface ProjectEntityRepository : JpaRepository<ProjectEntity, String>
