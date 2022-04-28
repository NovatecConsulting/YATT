package com.novatecgmbh.eventsourcing.axon.user.user.web.dto

import com.novatecgmbh.eventsourcing.axon.user.api.RegisterUserCommand
import com.novatecgmbh.eventsourcing.axon.user.api.UserId

data class RegisterUserDto(
    val firstname: String,
    val lastname: String,
    val email: String,
    val telephone: String
) {
  fun toCommand(externalUserId: String) =
      RegisterUserCommand(UserId(), externalUserId, firstname, lastname, email, telephone)
}
