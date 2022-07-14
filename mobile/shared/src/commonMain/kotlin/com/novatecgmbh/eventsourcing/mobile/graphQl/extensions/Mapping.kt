package com.novatecgmbh.eventsourcing.mobile.graphQl.extensions

import com.novatecgmbh.eventsourcing.mobile.data.projects.ParticipantResource
import com.novatecgmbh.eventsourcing.mobile.data.projects.ProjectResource
import com.novatecgmbh.eventsourcing.mobile.data.projects.Status
import com.novatecgmbh.eventsourcing.mobile.data.projects.TaskResource
import de.novatec_gmbh.graphql_kmm.apollo.ProjectQuery
import de.novatec_gmbh.graphql_kmm.apollo.type.ProjectStatus

fun ProjectQuery.Project.toResource(): ProjectResource {
    return ProjectResource(
        identifier,
        name,
        startDate,
        actualEndDate,
        if(status == ProjectStatus.DELAYED) Status.DELAYED else Status.ON_TIME
    )
}

fun ProjectQuery.Participant.toResource(): ParticipantResource {
    return ParticipantResource(
        identifier,
        user.firstname,
        user.lastname
    )
}

fun ProjectQuery.Task.toResource(): TaskResource {
    return TaskResource(
        identifier,
        name,
        startDate,
        endDate,
        status.toString()
    )
}