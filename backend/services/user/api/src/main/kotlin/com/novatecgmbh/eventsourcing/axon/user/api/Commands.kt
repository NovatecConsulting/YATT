package com.novatecgmbh.eventsourcing.axon.user.api

import org.axonframework.modelling.command.TargetAggregateIdentifier

abstract class UserCommand(
    open val aggregateIdentifier: UserId,
)

data class RegisterUserCommand(
    @TargetAggregateIdentifier override val aggregateIdentifier: UserId,
    val externalUserId: String,
    val firstname: String,
    val lastname: String,
    val email: String,
    val telephone: String
) : UserCommand(aggregateIdentifier)

data class RenameUserCommand(
    @TargetAggregateIdentifier override val aggregateIdentifier: UserId,
    val firstname: String,
    val lastname: String
) : UserCommand(aggregateIdentifier)
