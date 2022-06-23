package com.novatecgmbh.eventsourcing.mobile.data

import com.novatecgmbh.eventsourcing.mobile.Constants.restApiBaseUrl
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.serialization.Serializable

class UserClient(private val client: HttpClient) {
    suspend fun current(): UserResource = client.get("$restApiBaseUrl/v2/users/current").body()
}

@Serializable
data class UserResource(
    val identifier: String,
    val externalUserId: String,
    val firstname: String,
    val lastname: String,
    val email: String,
    val telephone: String
)