package com.novatecgmbh.eventsourcing.axon.application.config

import com.novatecgmbh.eventsourcing.axon.user.api.FindUserByExternalUserIdQuery
import com.novatecgmbh.eventsourcing.axon.user.api.UserId
import com.novatecgmbh.eventsourcing.axon.user.api.UserQueryResult
import org.axonframework.extensions.kotlin.queryOptional
import org.axonframework.queryhandling.QueryGateway
import org.springframework.core.convert.converter.Converter
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class UserProfileAuthenticationConverter(val queryGateway: QueryGateway) :
    Converter<Jwt, Mono<AbstractAuthenticationToken>> {
  private val converter = JwtAuthenticationConverter()

  override fun convert(source: Jwt): Mono<AbstractAuthenticationToken> {
    val token = converter.convert(source) as JwtAuthenticationToken?
    val externalUserId: String = source.getClaim("sub")

    val user =
        queryGateway
            .queryOptional<UserQueryResult, FindUserByExternalUserIdQuery>(
                FindUserByExternalUserIdQuery(externalUserId))
            .join()

    return if (token != null) {
      Mono.just(
          UserProfileAuthentication(
              token,
              user
                  .map { it.toRegisteredUserProfile() as UserProfile }
                  .orElse(UnregisteredUserProfile(externalUserId))),
      )
    } else {
      Mono.error { RuntimeException("JwtAuthenticationToken is null") }
    }
  }

  internal class UserProfileAuthentication(
      token: JwtAuthenticationToken,
      private val userProfile: UserProfile
  ) : JwtAuthenticationToken(token.token, token.authorities) {

    override fun getPrincipal(): UserProfile {
      return userProfile
    }
  }
}

interface UserProfile

data class RegisteredUserProfile(
    val identifier: UserId,
    val externalUserId: String,
    val firstname: String,
    val lastname: String
) : UserProfile

data class UnregisteredUserProfile(val externalUserId: String) : UserProfile

fun UserQueryResult.toRegisteredUserProfile() =
    RegisteredUserProfile(identifier, externalUserId, firstname, lastname)
