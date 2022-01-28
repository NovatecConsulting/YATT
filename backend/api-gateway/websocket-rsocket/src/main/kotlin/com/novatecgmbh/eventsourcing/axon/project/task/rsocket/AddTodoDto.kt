package com.novatecgmbh.eventsourcing.axon.project.task.rsocket

import com.novatecgmbh.eventsourcing.axon.project.task.api.AddTodoCommand
import com.novatecgmbh.eventsourcing.axon.project.task.api.TaskId
import com.novatecgmbh.eventsourcing.axon.project.task.api.TodoId

data class AddTodoDto(val taskId: TaskId, val todoId: TodoId = TodoId(), val description: String) {
  fun toCommand() = AddTodoCommand(identifier = taskId, todoId = todoId, description = description)
}
