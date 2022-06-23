package com.novatecgmbh.eventsourcing.mobile.domain

import com.novatecgmbh.eventsourcing.mobile.data.UserClient

class UserRepository(private val client: UserClient) {
    suspend fun current() = client.current().run {
        User(
            id = identifier,
            firstname = firstname,
            lastname = lastname,
            email = email,
            telephone = telephone
        )
    }
}

data class User(
    val id: String,
    val firstname: String,
    val lastname: String,
    val email: String,
    val telephone: String
)