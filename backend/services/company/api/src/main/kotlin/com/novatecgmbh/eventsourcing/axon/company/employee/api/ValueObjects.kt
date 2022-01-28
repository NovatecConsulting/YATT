package com.novatecgmbh.eventsourcing.axon.company.employee.api

import com.fasterxml.jackson.annotation.JsonValue
import java.io.Serializable
import java.util.*
import javax.persistence.Embeddable

@Embeddable
data class EmployeeId(@get:JsonValue val identifier: String) : Serializable {
  constructor() : this(UUID.randomUUID().toString())

  override fun toString(): String = identifier
}
