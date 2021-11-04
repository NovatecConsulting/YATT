package com.novatecgmbh.eventsourcing.axon.project.participant.api

import com.fasterxml.jackson.annotation.JsonValue
import java.io.Serializable
import java.util.*
import javax.persistence.Embeddable

@Embeddable
data class ParticipantId(@get:JsonValue val identifier: String) : Serializable {
  constructor() : this(UUID.randomUUID().toString())

  override fun toString(): String = identifier
}
