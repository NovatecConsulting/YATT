package com.novatecgmbh.eventsourcing.axon.project.task.api

import com.novatecgmbh.eventsourcing.axon.project.project.command.ProjectId
import com.novatecgmbh.eventsourcing.axon.project.task.command.TaskId

data class TasksByProjectQuery(val projectId: ProjectId)

data class TaskQuery(val taskId: TaskId)
