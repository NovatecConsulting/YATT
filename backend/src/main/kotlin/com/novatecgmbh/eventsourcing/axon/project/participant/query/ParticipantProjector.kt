package com.novatecgmbh.eventsourcing.axon.project.participant.query

import com.novatecgmbh.eventsourcing.axon.company.company.api.CompanyQuery
import com.novatecgmbh.eventsourcing.axon.company.company.api.CompanyQueryResult
import com.novatecgmbh.eventsourcing.axon.project.participant.api.ParticipantByProjectQuery
import com.novatecgmbh.eventsourcing.axon.project.participant.api.ParticipantCreatedEvent
import com.novatecgmbh.eventsourcing.axon.project.participant.api.ParticipantQuery
import com.novatecgmbh.eventsourcing.axon.project.participant.api.ParticipantQueryResult
import com.novatecgmbh.eventsourcing.axon.user.api.UserQuery
import com.novatecgmbh.eventsourcing.axon.user.api.UserQueryResult
import java.util.*
import org.axonframework.config.ProcessingGroup
import org.axonframework.eventhandling.EventHandler
import org.axonframework.eventhandling.ResetHandler
import org.axonframework.eventhandling.SequenceNumber
import org.axonframework.extensions.kotlin.emit
import org.axonframework.extensions.kotlin.queryOptional
import org.axonframework.queryhandling.QueryGateway
import org.axonframework.queryhandling.QueryHandler
import org.axonframework.queryhandling.QueryUpdateEmitter
import org.springframework.stereotype.Component

@Component
@ProcessingGroup("participant-projector")
class ParticipantProjector(
    private val repository: ParticipantProjectionRepository,
    private val queryUpdateEmitter: QueryUpdateEmitter,
    private val queryGateway: QueryGateway
) {
  @EventHandler
  fun on(event: ParticipantCreatedEvent, @SequenceNumber aggregateVersion: Long) {
    val user = queryGateway.queryOptional<UserQueryResult, UserQuery>(UserQuery(event.userId)).get()
    val company =
        queryGateway
            .queryOptional<CompanyQueryResult, CompanyQuery>(CompanyQuery(event.companyId))
            .get()
    saveProjection(
        ParticipantProjection(
            identifier = event.aggregateIdentifier,
            version = aggregateVersion,
            projectId = event.projectId,
            companyId = event.companyId,
            companyName = company.map { it.name }.orElse(null),
            userId = event.userId,
            userFirstName = user.map { it.firstname }.orElse(null),
            userLastName = user.map { it.lastname }.orElse(null)))
  }

  private fun saveProjection(projection: ParticipantProjection) {
    repository.save(projection).also { savedProjection -> updateQuerySubscribers(savedProjection) }
  }

  private fun updateQuerySubscribers(participant: ParticipantProjection) {
    queryUpdateEmitter.emit<ParticipantByProjectQuery, ParticipantQueryResult>(
        participant.toQueryResult()) { query -> query.projectId == participant.projectId }

    queryUpdateEmitter.emit<ParticipantQuery, ParticipantQueryResult>(
        participant.toQueryResult()) { query -> query.participantId == participant.identifier }
  }

  @ResetHandler fun reset() = repository.deleteAll()

  @QueryHandler
  fun handle(query: ParticipantQuery): Optional<ParticipantQueryResult> =
      repository.findById(query.participantId).map { it.toQueryResult() }

  @QueryHandler
  fun handle(query: ParticipantByProjectQuery): Iterable<ParticipantQueryResult> =
      repository.findAllByProjectId(query.projectId).map { it.toQueryResult() }
}
