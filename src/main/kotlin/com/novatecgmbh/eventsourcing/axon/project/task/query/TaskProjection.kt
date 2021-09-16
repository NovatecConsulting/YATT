package com.novatecgmbh.eventsourcing.axon.project.task.query

import com.novatecgmbh.eventsourcing.axon.project.project.command.ProjectId
import com.novatecgmbh.eventsourcing.axon.project.task.command.TaskId
import com.novatecgmbh.eventsourcing.axon.project.task.command.TaskStatusEnum
import java.time.LocalDate
import javax.persistence.*
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Entity
@Table(name = "tasks")
class TaskProjection(
    @EmbeddedId var identifier: TaskId,
    @Column(nullable = false) var version: Long,
    @Embedded
    @AttributeOverride(name = "identifier", column = Column(name = "projectId", nullable = false))
    var projectId: ProjectId,
    @Column(nullable = false) var name: String,
    var description: String?,
    @Column(nullable = false) var startDate: LocalDate,
    @Column(nullable = false) var endDate: LocalDate,
    @Column(nullable = false) var status: TaskStatusEnum
)
