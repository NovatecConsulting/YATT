package com.novatecgmbh.eventsourcing.axon.application.security

import org.springframework.core.convert.converter.Converter
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.oauth2.jwt.Jwt

class CustomUserAuthenticationConverter(val userDetailsService: UserDetailsService) :
    Converter<Jwt, AbstractAuthenticationToken> {
  override fun convert(jwt: Jwt): AbstractAuthenticationToken =
      try {
            userDetailsService.loadUserByUsername(jwt.claims["sub"] as String)
          } catch (ex: Exception) {
            UnregisteredUserPrincipal(jwt.claims["sub"] as String)
          }.let { UsernamePasswordAuthenticationToken(it, "n/a", it.authorities) }
}
