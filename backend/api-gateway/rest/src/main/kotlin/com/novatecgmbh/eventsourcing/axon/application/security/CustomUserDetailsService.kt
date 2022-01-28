package com.novatecgmbh.eventsourcing.axon.application.security

import com.novatecgmbh.eventsourcing.axon.user.api.FindUserByExternalUserIdQuery
import com.novatecgmbh.eventsourcing.axon.user.api.UserQueryResult
import kotlin.jvm.Throws
import org.axonframework.extensions.kotlin.queryOptional
import org.axonframework.queryhandling.QueryGateway
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.stereotype.Service

@Service
class CustomUserDetailsService(val queryGateway: QueryGateway) : UserDetailsService {
  @Throws(Exception::class)
  override fun loadUserByUsername(username: String): UserDetails {
    SecurityContextHelper.setAuthentication("system") // TODO ?
    return queryGateway
        .queryOptional<UserQueryResult, FindUserByExternalUserIdQuery>(
            FindUserByExternalUserIdQuery(username))
        .join()
        .get()
        .toRegisteredUser()
  }
}

fun UserQueryResult.toRegisteredUser() =
    RegisteredUserPrincipal(identifier, externalUserId, firstname, lastname)
