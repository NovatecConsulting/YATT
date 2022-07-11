package com.novatecgmbh.eventsourcing.mobile.graphQl

import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.network.http.DefaultHttpEngine
import com.novatecgmbh.eventsourcing.mobile.Constants
import com.russhwolf.settings.Settings
import de.novatec_gmbh.graphql_kmm.apollo.ProjectQuery
import de.novatec_gmbh.graphql_kmm.apollo.ProjectsQuery
import de.novatec_gmbh.graphql_kmm.apollo.type.Query
import io.ktor.client.*

class GraphQlClient(private val settings: Settings) {

    private val apolloClient = ApolloClient.Builder()
        .serverUrl(Constants.graphQlUrl)
        .build()

    suspend fun getProjects(): List<ProjectsQuery.Project?> {
        val query = apolloClient
            .query(ProjectsQuery())
            .addHttpHeader("Authorization",
                "Bearer ${settings.getString(Constants.settingsAccessTokenKey)}")
        val response = query.execute()
        return response.data?.projects ?: listOf()
    }

    suspend fun getProject(id: String): ProjectQuery.Project? {
        val query = apolloClient
            .query(ProjectQuery(id))
            .addHttpHeader("Authorization",
                "Bearer ${settings.getString(Constants.settingsAccessTokenKey)}")
        val response = query.execute()
        return response.data?.project
    }

}