package com.novatecgmbh.eventsourcing.axon.project.task.query

import com.novatecgmbh.eventsourcing.axon.project.project.api.ProjectId
import com.novatecgmbh.eventsourcing.axon.project.task.api.*
import java.io.Serializable
import java.time.LocalDate
import javax.persistence.*
import javax.persistence.CascadeType.ALL
import javax.persistence.FetchType.EAGER

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
    @Column(nullable = false) var status: TaskStatusEnum,
    @OneToMany(cascade = [ALL], orphanRemoval = true, fetch = EAGER)
    @JoinColumn(name = "taskId")
    var todos: MutableList<Todo>
) {
  fun toQueryResult() =
      TaskQueryResult(
          identifier,
          projectId,
          name,
          description,
          startDate,
          endDate,
          status,
          todos.map { it.toQueryResult() })
}

@Entity
@Table(name = "task_todos")
class Todo(@EmbeddedId var key: TodoKey, var description: String, var isDone: Boolean) {
  fun toQueryResult() = TodoQueryResult(key.todoId, description, isDone)
}

@Embeddable
data class TodoKey(
    @Embedded
    @AttributeOverride(name = "identifier", column = Column(name = "todoId", nullable = false))
    var todoId: TodoId,
    @Embedded
    @AttributeOverride(name = "identifier", column = Column(name = "taskId", nullable = false))
    var taskId: TaskId,
) : Serializable
