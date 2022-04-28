package com.novatecgmbh.eventsourcing.axon.user.api

class AllUsersQuery

data class FindUserByExternalUserIdQuery(val externalUserId: String)

data class UserQuery(val userId: UserId)

data class UserQueryResult(
    val identifier: UserId,
    val externalUserId: String,
    val firstname: String,
    val lastname: String,
    val email: String,
    val telephone: String
)
