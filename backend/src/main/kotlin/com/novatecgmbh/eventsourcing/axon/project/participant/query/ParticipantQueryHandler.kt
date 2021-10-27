package com.novatecgmbh.eventsourcing.axon.project.participant.query

import com.novatecgmbh.eventsourcing.axon.application.auditing.AUDIT_USER_ID_META_DATA_KEY
import com.novatecgmbh.eventsourcing.axon.project.authorization.ProjectAuthorizationService
import com.novatecgmbh.eventsourcing.axon.project.participant.api.ParticipantByProjectQuery
import com.novatecgmbh.eventsourcing.axon.project.participant.api.ParticipantQuery
import com.novatecgmbh.eventsourcing.axon.project.participant.api.ParticipantQueryResult
import com.novatecgmbh.eventsourcing.axon.user.api.UserId
import java.util.*
import org.axonframework.messaging.annotation.MetaDataValue
import org.axonframework.queryhandling.QueryHandler
import org.springframework.stereotype.Component

@Component
class ParticipantQueryHandler(
    private val repository: ParticipantProjectionRepository,
    private val authService: ProjectAuthorizationService
) {

  @QueryHandler
  fun handle(
      query: ParticipantQuery,
      @MetaDataValue(AUDIT_USER_ID_META_DATA_KEY) userId: String
  ): Optional<ParticipantQueryResult> =
      repository.findById(query.participantId).map { it.toQueryResult() }?.let {
        authService.runWhenAuthorizedForProject(UserId(userId), it.get().projectId) { it }
      }
          ?: Optional.empty()

  @QueryHandler
  fun handle(
      query: ParticipantByProjectQuery,
      @MetaDataValue(AUDIT_USER_ID_META_DATA_KEY) userId: String
  ): Iterable<ParticipantQueryResult> =
      authService.runWhenAuthorizedForProject(UserId(userId), query.projectId) {
        repository.findAllByProjectId(query.projectId).map { it.toQueryResult() }
      }
}
