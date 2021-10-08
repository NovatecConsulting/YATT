package com.novatecgmbh.eventsourcing.axon.security

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

data class UnregisteredUserPrincipal(private val username: String) : UserDetails {
  override fun getAuthorities(): MutableCollection<out GrantedAuthority> = mutableSetOf()

  override fun getPassword(): String? = null

  override fun getUsername(): String = username

  override fun isAccountNonExpired() = true

  override fun isAccountNonLocked() = true

  override fun isCredentialsNonExpired() = true

  override fun isEnabled() = true
}
