package com.novatecgmbh.eventsourcing.axon.user.command.view

import com.novatecgmbh.eventsourcing.axon.project.participant.api.ParticipantId
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UserUniqueKeyRepository : JpaRepository<UserUniqueKeyProjection, ParticipantId> {
  fun existsByExternalUserId(externalUserId: String): Boolean
}
