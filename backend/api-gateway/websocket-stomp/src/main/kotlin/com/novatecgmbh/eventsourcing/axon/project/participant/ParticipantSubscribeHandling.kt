package com.novatecgmbh.eventsourcing.axon.project.participant

import com.novatecgmbh.eventsourcing.axon.application.security.RegisteredUserPrincipal
import com.novatecgmbh.eventsourcing.axon.application.websocket.BaseSubscribeHandling
import com.novatecgmbh.eventsourcing.axon.application.websocket.CurrentUserSubscriptionQueries
import com.novatecgmbh.eventsourcing.axon.project.participant.api.ParticipantByProjectQuery
import com.novatecgmbh.eventsourcing.axon.project.participant.api.ParticipantId
import com.novatecgmbh.eventsourcing.axon.project.participant.api.ParticipantQuery
import com.novatecgmbh.eventsourcing.axon.project.participant.api.ParticipantQueryResult
import com.novatecgmbh.eventsourcing.axon.project.project.api.*
import org.axonframework.messaging.responsetypes.ResponseTypes
import org.axonframework.queryhandling.QueryGateway
import org.axonframework.queryhandling.SubscriptionQueryResult
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Component
import org.springframework.util.AntPathMatcher

@Component
class ParticipantSubscribeHandling(
    currentUserSubscriptions: CurrentUserSubscriptionQueries,
    messagingTemplate: SimpMessagingTemplate,
    val queryGateway: QueryGateway,
) : BaseSubscribeHandling(currentUserSubscriptions, messagingTemplate) {
  override fun mapToSubscriptionQuery(
      destination: String,
      user: RegisteredUserPrincipal
  ): SubscriptionQueryResult<*, *>? {
    val pathMatcher = AntPathMatcher()
    val parts = destination.substring(1).split("/")
    return when {
      pathMatcher.match("/projects/{projectId}/participants", destination) ->
          queryGateway.subscriptionQuery(
              ParticipantByProjectQuery(ProjectId(parts.component2())),
              ResponseTypes.multipleInstancesOf(ParticipantQueryResult::class.java),
              ResponseTypes.instanceOf(ParticipantQueryResult::class.java),
          )
      pathMatcher.match("/participants/{participantId}", destination) ->
          queryGateway.subscriptionQuery(
              ParticipantQuery(ParticipantId(parts.component2())),
              ResponseTypes.instanceOf(ParticipantQueryResult::class.java),
              ResponseTypes.instanceOf(ParticipantQueryResult::class.java),
          )
      else -> null
    }
  }
}
