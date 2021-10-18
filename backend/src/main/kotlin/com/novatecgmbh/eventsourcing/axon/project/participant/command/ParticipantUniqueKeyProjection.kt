package com.novatecgmbh.eventsourcing.axon.project.participant.command

import com.novatecgmbh.eventsourcing.axon.company.company.api.CompanyId
import com.novatecgmbh.eventsourcing.axon.project.participant.api.ParticipantId
import com.novatecgmbh.eventsourcing.axon.project.project.api.ProjectId
import com.novatecgmbh.eventsourcing.axon.user.api.UserId
import javax.persistence.*

@Entity
@Table(
    name = "participant_unique_key",
    uniqueConstraints = [UniqueConstraint(columnNames = ["companyId", "userId"])])
class ParticipantUniqueKeyProjection(
    @EmbeddedId var identifier: ParticipantId,
    @Embedded
    @AttributeOverride(name = "identifier", column = Column(name = "projectId", nullable = false))
    var projectId: ProjectId,
    @Embedded
    @AttributeOverride(name = "identifier", column = Column(name = "companyId", nullable = false))
    var companyId: CompanyId,
    @Embedded
    @AttributeOverride(name = "identifier", column = Column(name = "userId", nullable = false))
    var userId: UserId,
)
