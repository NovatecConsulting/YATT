package com.novatecgmbh.eventsourcing.axon.project.task.rsocket

import com.novatecgmbh.eventsourcing.axon.project.task.api.MarkTodoAsDoneCommand
import com.novatecgmbh.eventsourcing.axon.project.task.api.TaskId
import com.novatecgmbh.eventsourcing.axon.project.task.api.TodoId

data class MarkTodoAsDoneDto(val taskId: TaskId, val todoId: TodoId) {
    fun toCommand() = MarkTodoAsDoneCommand(identifier = taskId, todoId = todoId)
}