package com.novatecgmbh.eventsourcing.axon.project.task.api

import com.novatecgmbh.eventsourcing.axon.project.project.api.ProjectId

data class TasksByProjectQuery(val projectId: ProjectId)

data class TaskQuery(val taskId: TaskId)
