package com.novatecgmbh.eventsourcing.axon.application.security

import com.novatecgmbh.eventsourcing.axon.user.api.UserId

interface UserProfile

data class RegisteredUserProfile(
    val identifier: UserId,
    val externalUserId: String,
    val firstname: String,
    val lastname: String
) : UserProfile

data class UnregisteredUserProfile(val externalUserId: String) : UserProfile
