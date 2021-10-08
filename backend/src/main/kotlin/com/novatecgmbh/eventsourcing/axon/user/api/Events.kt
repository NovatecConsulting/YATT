package com.novatecgmbh.eventsourcing.axon.user.api

abstract class UserEvent(
    open val aggregateIdentifier: UserId,
)

data class UserRegisteredEvent(
    override val aggregateIdentifier: UserId,
    val externalUserId: String,
    val firstname: String,
    val lastname: String,
) : UserEvent(aggregateIdentifier)
