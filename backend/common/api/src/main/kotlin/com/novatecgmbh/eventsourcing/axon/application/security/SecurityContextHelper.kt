package com.novatecgmbh.eventsourcing.axon.application.security

import com.novatecgmbh.eventsourcing.axon.user.api.UserId
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder

object SecurityContextHelper {
  fun setAuthentication(userId: String) {
    SecurityContextHolder.getContext().authentication =
        UsernamePasswordAuthenticationToken(
            RegisteredUserPrincipal(UserId(userId), "", "", ""), null)
  }
}
