package com.novatecgmbh.eventsourcing.mobile.di

import com.novatecgmbh.eventsourcing.mobile.domain.AuthRepository
import com.novatecgmbh.eventsourcing.mobile.Constants
import com.novatecgmbh.eventsourcing.mobile.data.AuthClient
import com.novatecgmbh.eventsourcing.mobile.data.projects.ProjectClient
import com.novatecgmbh.eventsourcing.mobile.data.UserClient
import com.novatecgmbh.eventsourcing.mobile.domain.UserRepository
import com.novatecgmbh.eventsourcing.mobile.graphQl.GraphQlClient
import com.novatecgmbh.eventsourcing.mobile.graphQl.AuthenticationInterceptor
import com.russhwolf.settings.Settings
import io.ktor.client.HttpClient
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.*
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

expect val platformModule: Module

fun initKoin(appDeclaration: KoinAppDeclaration = {}): KoinApplication = startKoin {
    appDeclaration()
    modules(
        platformModule,
        commonModule
    )
}

var commonModule = module {
    single { createClient(get()) }
    single { AuthClient(get()) }
    single { AuthRepository(get(), get()) }
    single { UserClient(get()) }
    single { UserRepository(get()) }
    single { AuthenticationInterceptor(get()) }
    single { GraphQlClient(get(), get()) }
    single { ProjectClient(get()) }
}

fun createClient(settings: Settings) = HttpClient {
    expectSuccess = true

    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
        })
    }

    install(Logging) {
        logger = object : Logger {
            override fun log(message: String) {
                println(message)
            }
        }
        level = LogLevel.INFO
    }

    install(Auth) {
        bearer {
            loadTokens {
                BearerTokens(
                    accessToken = settings.getString(Constants.settingsAccessTokenKey),
                    refreshToken = settings.getString(
                        Constants.settingsRefreshTokenKey
                    )
                )
            }
            sendWithoutRequest { request ->
                request.url.port == 8080
            }
        }
    }
}