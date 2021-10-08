package com.novatecgmbh.eventsourcing.axon.user.web.dto

import com.novatecgmbh.eventsourcing.axon.user.api.RegisterUserCommand
import com.novatecgmbh.eventsourcing.axon.user.api.UserId

data class RegisterUserDto(val firstname: String, val lastname: String) {
  fun toCommand(externalUserId: String) =
      RegisterUserCommand(UserId(), externalUserId, firstname, lastname)
}
