package com.novatecgmbh.eventsourcing.mobile.data.projects

import io.ktor.client.*
import io.ktor.client.request.*
import kotlinx.serialization.Serializable
import com.novatecgmbh.eventsourcing.mobile.Constants.restApiBaseUrl
import io.ktor.client.call.*

class ProjectClient(private val client: HttpClient) {

    suspend fun getProject(identifier: String): ProjectResource =
        client.get("$restApiBaseUrl/v2/projects/$identifier").body()

    suspend fun getParticipants(projectId: String): List<ParticipantResource> =
        client.get("${restApiBaseUrl}/v2/projects/$projectId/participants").body()

    suspend fun getTasks(projectId: String): List<TaskResource> =
        client.get("${restApiBaseUrl}/v2/projects/$projectId/tasks").body()
}

@Serializable
data class ProjectResource(
    val identifier: String,
    val name: String,
    val startDate: String?,
    val actualEndDate: String?,
    val status: Status,
)

enum class Status{
    ON_TIME, DELAYED
}

@Serializable
data class ParticipantResource(
    val identifier: String,
    val userFirstName: String,
    val userLastName: String
)

@Serializable
data class TaskResource(
    val identifier: String,
    val name: String,
    val startDate: String?,
    val endDate: String?,
    val status: String
)