package com.novatecgmbh.eventsourcing.mobile.data

import com.novatecgmbh.eventsourcing.mobile.Constants
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.*
import io.ktor.client.request.forms.submitForm
import io.ktor.http.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

class AuthClient(private val client: HttpClient) {
    suspend fun login(username: String, password: String): TokenResponse = client.submitForm(
        url = Constants.tokenUrl,
        formParameters = Parameters.build {
            append("grant_type", "password")
            append("client_id", "mobile")
            append("username", username)
            append("password", password)
        }
    ) {
       headers {
        append(HttpHeaders.Host, "localhost:8999")
    }}.body()
}

@Serializable
data class TokenResponse(
    @SerialName("access_token") val accessToken: String,
    @SerialName("expires_in") val expiresIn: Long,
    @SerialName("refresh_expires_in") val refreshExpiresIn: Long,
    @SerialName("refresh_token") val refreshToken: String,
    @SerialName("token_type") val tokenType: String,
    @SerialName("not-before-policy") val notBeforePolicy: Int,
    @SerialName("session_state") val sessionState: String,
    @SerialName("scope") val scope: String
)