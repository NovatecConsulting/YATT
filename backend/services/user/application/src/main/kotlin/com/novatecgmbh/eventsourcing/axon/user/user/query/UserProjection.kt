package com.novatecgmbh.eventsourcing.axon.user.user.query

import com.novatecgmbh.eventsourcing.axon.user.api.UserId
import com.novatecgmbh.eventsourcing.axon.user.api.UserQueryResult
import javax.persistence.Column
import javax.persistence.EmbeddedId
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "users")
class UserProjection(
    @EmbeddedId var identifier: UserId,
    @Column(nullable = false) var externalUserId: String,
    @Column(nullable = false) var firstname: String,
    @Column(nullable = false) var lastname: String
) {
  fun toQueryResult() = UserQueryResult(identifier, externalUserId, firstname, lastname)
}