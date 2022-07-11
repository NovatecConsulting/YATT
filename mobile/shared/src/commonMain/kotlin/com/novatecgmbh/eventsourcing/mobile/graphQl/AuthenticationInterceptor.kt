package com.novatecgmbh.eventsourcing.mobile.graphQl

import com.apollographql.apollo3.api.http.HttpRequest
import com.apollographql.apollo3.api.http.HttpResponse
import com.apollographql.apollo3.network.http.HttpInterceptor
import com.apollographql.apollo3.network.http.HttpInterceptorChain
import com.novatecgmbh.eventsourcing.mobile.Constants
import com.russhwolf.settings.Settings

class AuthenticationInterceptor(private val settings: Settings): HttpInterceptor {

    override suspend fun intercept(
        request: HttpRequest,
        chain: HttpInterceptorChain
    ): HttpResponse {
        val token = settings.getString(Constants.settingsAccessTokenKey)
        return chain.proceed(request.newBuilder().addHeader("Authorization", "Bearer $token").build())
    }

}