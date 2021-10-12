package com.novatecgmbh.eventsourcing.axon.application.security

import com.novatecgmbh.eventsourcing.axon.user.api.UserId
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

data class RegisteredUserPrincipal(
    val identifier: UserId,
    val externalUserId: String,
    val firstname: String,
    val lastname: String,
) : UserDetails {
  override fun getAuthorities(): MutableCollection<out GrantedAuthority> = mutableSetOf()

  override fun getPassword(): String? = null

  override fun getUsername(): String = externalUserId

  override fun isAccountNonExpired(): Boolean = true

  override fun isAccountNonLocked(): Boolean = true

  override fun isCredentialsNonExpired(): Boolean = true

  override fun isEnabled(): Boolean = true
}
