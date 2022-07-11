package com.novatecgmbh.eventsourcing.mobile.graphQl

import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.interceptor.ApolloInterceptor
import com.apollographql.apollo3.network.http.DefaultHttpEngine
import com.novatecgmbh.eventsourcing.mobile.Constants
import com.russhwolf.settings.Settings
import de.novatec_gmbh.graphql_kmm.apollo.CreateProjectMutation
import de.novatec_gmbh.graphql_kmm.apollo.ProjectQuery
import de.novatec_gmbh.graphql_kmm.apollo.ProjectsQuery
import de.novatec_gmbh.graphql_kmm.apollo.type.Date
import de.novatec_gmbh.graphql_kmm.apollo.type.Query
import io.ktor.client.*

class GraphQlClient(private val settings: Settings, private val httpInterceptor: AuthenticationInterceptor) {

    private val apolloClient = ApolloClient.Builder()
        .serverUrl(Constants.graphQlUrl)
        .addHttpInterceptor(httpInterceptor)
        .build()

    suspend fun getProjects(): List<ProjectsQuery.Project?> {
        val query = apolloClient
            .query(ProjectsQuery())
        val response = query.execute()
        return response.data?.projects ?: listOf()
    }

    suspend fun getProject(id: String): ProjectQuery.Project? {
        val query = apolloClient
            .query(ProjectQuery(id))
        val response = query.execute()
        return response.data?.project
    }

    suspend fun createProject(projectName: String, startDate: String, deadLine: String, companyId: String): CreateProjectMutation.Data? {
        val mutation = apolloClient
            .mutation(CreateProjectMutation(projectName, startDate, deadLine, companyId))
        val response = mutation.execute()
        return response.data
    }

}