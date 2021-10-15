package com.novatecgmbh.eventsourcing.axon.project.participant.query

import com.novatecgmbh.eventsourcing.axon.company.company.api.CompanyId
import com.novatecgmbh.eventsourcing.axon.project.participant.api.ParticipantId
import com.novatecgmbh.eventsourcing.axon.project.participant.api.ParticipantQueryResult
import com.novatecgmbh.eventsourcing.axon.project.project.api.ProjectId
import com.novatecgmbh.eventsourcing.axon.user.api.UserId
import javax.persistence.*

@Entity
@Table(name = "participant")
class ParticipantProjection(
    @EmbeddedId var identifier: ParticipantId,
    @Column(nullable = false) var version: Long,
    @Embedded
    @AttributeOverride(name = "identifier", column = Column(name = "projectId", nullable = false))
    var projectId: ProjectId,
    @Embedded
    @AttributeOverride(name = "identifier", column = Column(name = "companyId", nullable = false))
    var companyId: CompanyId,
    var companyName: String?,
    @Embedded
    @AttributeOverride(name = "identifier", column = Column(name = "userId", nullable = false))
    var userId: UserId,
    var userFirstName: String?,
    var userLastName: String?
) {
  fun toQueryResult() =
      ParticipantQueryResult(
          identifier,
          version,
          projectId,
          companyId,
          companyName,
          userId,
          userFirstName,
          userLastName)
}
