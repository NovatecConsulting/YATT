package com.novatecgmbh.eventsourcing.axon.project.project.api

import com.novatecgmbh.eventsourcing.axon.project.project.command.ProjectId

class AllProjectsQuery

data class ProjectQuery(val projectId: ProjectId)
