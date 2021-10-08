package com.novatecgmbh.eventsourcing.axon.security

import com.novatecgmbh.eventsourcing.axon.user.query.UserProjectionRepository
import kotlin.jvm.Throws
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.stereotype.Service

@Service
class CustomUserDetailsService(val userProjectionRepository: UserProjectionRepository) :
    UserDetailsService {
  @Throws(Exception::class)
  override fun loadUserByUsername(username: String): UserDetails =
      userProjectionRepository.findByExternalUserId(username).get().toRegisteredUser()
}
