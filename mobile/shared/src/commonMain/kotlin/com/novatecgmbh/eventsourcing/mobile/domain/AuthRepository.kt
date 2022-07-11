package com.novatecgmbh.eventsourcing.mobile.domain

import com.apollographql.apollo3.ApolloClient
import com.novatecgmbh.eventsourcing.mobile.Constants
import com.novatecgmbh.eventsourcing.mobile.data.AuthClient
import com.russhwolf.settings.Settings
import com.russhwolf.settings.set
import de.novatec_gmbh.graphql_kmm.apollo.ProjectQuery

class AuthRepository(private val client: AuthClient, private val settings: Settings) {
    suspend fun login(username: String, password: String) {
        client.login(username, password).also {
            settings[Constants.settingsAccessTokenKey] = it.accessToken
            settings[Constants.settingsRefreshTokenKey] = it.refreshToken
        }
    }

    fun isLoggedIn(): Boolean = settings.getString(Constants.settingsAccessTokenKey).isNotEmpty()
}