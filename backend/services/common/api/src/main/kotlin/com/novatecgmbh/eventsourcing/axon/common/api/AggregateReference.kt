package com.novatecgmbh.eventsourcing.axon.common.api

import javax.persistence.Embeddable
import javax.persistence.Embedded

@Embeddable
data class AggregateReference<T>(@Embedded val identifier: T, val displayName: String? = null)
