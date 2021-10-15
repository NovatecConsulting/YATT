package com.novatecgmbh.eventsourcing.axon.user.web.dto

import com.novatecgmbh.eventsourcing.axon.user.api.RenameUserCommand
import com.novatecgmbh.eventsourcing.axon.user.api.UserId

data class RenameUserDto(val firstname: String, val lastname: String) {
  fun toCommand(identifier: UserId) = RenameUserCommand(identifier, firstname, lastname)
}
