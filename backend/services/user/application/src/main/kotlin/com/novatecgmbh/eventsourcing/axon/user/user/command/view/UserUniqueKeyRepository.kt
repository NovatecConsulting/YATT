package com.novatecgmbh.eventsourcing.axon.user.user.command.view

import com.novatecgmbh.eventsourcing.axon.user.api.UserId
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UserUniqueKeyRepository : JpaRepository<UserUniqueKeyProjection, UserId> {
  fun existsByExternalUserId(externalUserId: String): Boolean
}
