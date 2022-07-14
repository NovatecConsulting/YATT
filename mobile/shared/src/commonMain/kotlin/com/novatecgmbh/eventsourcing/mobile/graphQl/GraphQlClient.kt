package com.novatecgmbh.eventsourcing.mobile.graphQl

import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.adapter.KotlinxLocalDateAdapter
import com.novatecgmbh.eventsourcing.mobile.Constants
import com.russhwolf.settings.Settings
import de.novatec_gmbh.graphql_kmm.apollo.*
import de.novatec_gmbh.graphql_kmm.apollo.type.Date
import kotlinx.datetime.LocalDate

class GraphQlClient(private val settings: Settings, private val httpInterceptor: AuthenticationInterceptor) {

    private val apolloClient = ApolloClient.Builder()
        .serverUrl(Constants.graphQlUrl)
        .addHttpInterceptor(httpInterceptor)
        .addCustomScalarAdapter(Date.type, KotlinxLocalDateAdapter)
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

    suspend fun getCompanies(): List<CompaniesQuery.Company?> {
        val query = apolloClient
            .query(CompaniesQuery())
        val response = query.execute()
        return response.data?.companies ?: listOf()
    }

    suspend fun createProject(projectName: String, startDate: String, deadLine: String, companyId: String): Boolean {
        val mutation = apolloClient
            .mutation(CreateProjectMutation(
                projectName,
                LocalDate.parse(startDate),
                LocalDate.parse(deadLine),
                companyId))
        val response = mutation.execute()
        return response.data != null
    }

    suspend fun renameUser(identifier: String, firstname: String, lastname: String): Boolean {
        val mutation = apolloClient
            .mutation(RenameUserMutation(identifier, firstname, lastname))
        val response = mutation.execute()
        return response.data != null
    }

}