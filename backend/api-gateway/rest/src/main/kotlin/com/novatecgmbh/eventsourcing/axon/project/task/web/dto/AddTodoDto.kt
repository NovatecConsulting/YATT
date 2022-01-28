package com.novatecgmbh.eventsourcing.axon.project.task.web.dto

import com.novatecgmbh.eventsourcing.axon.project.task.api.AddTodoCommand
import com.novatecgmbh.eventsourcing.axon.project.task.api.TaskId
import com.novatecgmbh.eventsourcing.axon.project.task.api.TodoId

data class AddTodoDto(val description: String) {
  fun toCommand(taskId: TaskId) =
      AddTodoCommand(identifier = taskId, todoId = TodoId(), description = description)
}
