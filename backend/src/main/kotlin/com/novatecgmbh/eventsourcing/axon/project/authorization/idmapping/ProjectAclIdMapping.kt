package com.novatecgmbh.eventsourcing.axon.project.authorization.idmapping

import com.novatecgmbh.eventsourcing.axon.project.ProjectContextAggregateTypesEnum
import com.novatecgmbh.eventsourcing.axon.project.project.api.ProjectId
import java.io.Serializable
import javax.persistence.*
import javax.persistence.EnumType.STRING

@Entity
@Table(name = "project_acls_id_mapping")
class ProjectAclIdMapping(@EmbeddedId val key: ProjectAclIdMappingKey)

@Embeddable
class ProjectAclIdMappingKey(
    @Enumerated(STRING) var aggregateType: ProjectContextAggregateTypesEnum,
    var aggregateIdentifier: String,
    @Embedded var projectId: ProjectId
) : Serializable {
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as ProjectAclIdMappingKey

    if (aggregateType != other.aggregateType) return false
    if (aggregateIdentifier != other.aggregateIdentifier) return false
    if (projectId != other.projectId) return false

    return true
  }

  override fun hashCode(): Int {
    var result = aggregateType.hashCode()
    result = 31 * result + aggregateIdentifier.hashCode()
    result = 31 * result + projectId.hashCode()
    return result
  }
}
