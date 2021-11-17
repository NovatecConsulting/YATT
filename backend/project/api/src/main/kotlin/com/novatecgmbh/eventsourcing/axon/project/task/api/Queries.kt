package com.novatecgmbh.eventsourcing.axon.project.task.api

import com.novatecgmbh.eventsourcing.axon.project.project.api.ProjectId
import java.io.Serializable
import java.time.LocalDate
import javax.persistence.*

data class TasksByProjectQuery(val projectId: ProjectId)

data class TaskQuery(val taskId: TaskId)

data class TaskQueryResult(
    val identifier: TaskId,
    val projectId: ProjectId,
    val name: String,
    val description: String?,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val status: TaskStatusEnum,
    val todos: List<Todo>,
)

@Embeddable
data class Todo(
    @Embedded
    @AttributeOverride(name = "identifier", column = Column(name = "todoId", nullable = false))
    val todoId: TodoId,
    val description: String,
    var isDone: Boolean
) : Serializable
