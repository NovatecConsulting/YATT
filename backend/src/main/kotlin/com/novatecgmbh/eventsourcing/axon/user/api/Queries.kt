package com.novatecgmbh.eventsourcing.axon.user.api

data class FindUserByExternalUserIdQuery(val externalUserId: String)

data class UserQueryResult(
    val identifier: UserId,
    val externalUserId: String,
    val firstname: String,
    val lastname: String,
)
