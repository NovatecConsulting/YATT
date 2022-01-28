package com.novatecgmbh.eventsourcing.axon.user.user.query

import com.novatecgmbh.eventsourcing.axon.user.api.UserId
import java.util.*
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UserProjectionRepository : JpaRepository<UserProjection, UserId> {
  fun findByExternalUserId(externalUserId: String): Optional<UserProjection>
}
