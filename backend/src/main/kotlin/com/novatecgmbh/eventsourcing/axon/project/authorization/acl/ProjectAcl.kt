package com.novatecgmbh.eventsourcing.axon.project.authorization.acl

import com.novatecgmbh.eventsourcing.axon.user.api.UserId
import java.io.Serializable
import javax.persistence.*
import javax.persistence.EnumType.STRING

@Entity @Table(name = "project_acls") class ProjectAcl(@EmbeddedId var key: ProjectAclKey)

@Embeddable
class ProjectAclKey(
    @Enumerated(STRING) var aggregateType: AuthorizableAggregateTypesEnum,
    var aggregateIdentifier: String,
    @Embedded var userId: UserId,
    @Enumerated(STRING) var permission: PermissionEnum
) : Serializable {
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as ProjectAclKey

    if (aggregateType != other.aggregateType) return false
    if (aggregateIdentifier != other.aggregateIdentifier) return false
    if (userId != other.userId) return false
    if (permission != other.permission) return false

    return true
  }

  override fun hashCode(): Int {
    var result = aggregateType.hashCode()
    result = 31 * result + aggregateIdentifier.hashCode()
    result = 31 * result + userId.hashCode()
    result = 31 * result + permission.hashCode()
    return result
  }
}

enum class AuthorizableAggregateTypesEnum {
  PROJECT,
  COMPANY
}

enum class PermissionEnum {
  CREATE_PROJECT,
  ACCESS_PROJECT
}
