package com.novatecgmbh.eventsourcing.axon.common.query

import javax.persistence.Embeddable
import javax.persistence.Embedded

@Embeddable
data class AggregateReference<T>(@Embedded val identifier: T, val displayName: String? = null)
