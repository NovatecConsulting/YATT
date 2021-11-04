package com.novatecgmbh.eventsourcing.axon.common.references

import java.io.Serializable
import javax.persistence.*

@Entity
@Table(name = "root_context_id_mapping")
class RootContextIdMapping(@EmbeddedId val key: RootContextIdMappingKey)

@Embeddable
class RootContextIdMappingKey(
    var aggregateType: String,
    var aggregateIdentifier: String,
    var rootContextId: String
) : Serializable {
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as RootContextIdMappingKey

    if (aggregateType != other.aggregateType) return false
    if (aggregateIdentifier != other.aggregateIdentifier) return false
    if (rootContextId != other.rootContextId) return false

    return true
  }

  override fun hashCode(): Int {
    var result = aggregateType.hashCode()
    result = 31 * result + aggregateIdentifier.hashCode()
    result = 31 * result + rootContextId.hashCode()
    return result
  }
}
