package com.novatecgmbh.eventsourcing.axon.project.task.query

import com.novatecgmbh.eventsourcing.axon.project.project.api.ProjectId
import com.novatecgmbh.eventsourcing.axon.project.task.api.*
import java.io.Serializable
import java.time.LocalDate
import javax.persistence.*
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
    @ElementCollection(fetch = EAGER)
    @CollectionTable(
        name = "task_todos",
        foreignKey = ForeignKey(name = "FK_TaskTodos_TaskIdentifier"),
        joinColumns = [JoinColumn(name = "task_identifier")],
        uniqueConstraints =
            [
                UniqueConstraint(
                    name = "UK_TaskTodos_Identifier",
                    columnNames = ["identifier", "task_identifier"],
                ),
            ],
    )
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

@Embeddable
data class Todo(
    @Embedded @Column(nullable = false) val todoId: TodoId,
    @Column(nullable = false) val description: String,
    @Column(nullable = false) var isDone: Boolean
) : Serializable {
  fun toQueryResult() = TodoQueryResult(todoId, description, isDone)
}
