package com.novatecgmbh.eventsourcing.axon.user.user.command.view

import com.novatecgmbh.eventsourcing.axon.user.api.UserId
import javax.persistence.Column
import javax.persistence.EmbeddedId
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "user_unique_key")
class UserUniqueKeyProjection(
    @EmbeddedId var identifier: UserId,
    @Column(nullable = false, unique = true) var externalUserId: String,
    @Column(nullable = false, unique = true) var email: String
)
