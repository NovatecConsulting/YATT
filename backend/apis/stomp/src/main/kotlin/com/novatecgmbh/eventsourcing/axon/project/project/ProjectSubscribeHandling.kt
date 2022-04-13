package com.novatecgmbh.eventsourcing.axon.project.project

import com.novatecgmbh.eventsourcing.axon.application.security.RegisteredUserPrincipal
import com.novatecgmbh.eventsourcing.axon.application.websocket.BaseSubscribeHandling
import com.novatecgmbh.eventsourcing.axon.application.websocket.CurrentUserSubscriptionQueries
import com.novatecgmbh.eventsourcing.axon.project.project.api.*
import org.axonframework.messaging.responsetypes.ResponseTypes
import org.axonframework.queryhandling.QueryGateway
import org.axonframework.queryhandling.SubscriptionQueryResult
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Component
import org.springframework.util.AntPathMatcher

@Component
class ProjectSubscribeHandling(
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
      pathMatcher.match("/projects", destination) ->
          queryGateway.subscriptionQuery(
              MyProjectsQuery(user.identifier),
              ResponseTypes.multipleInstancesOf(ProjectQueryResult::class.java),
              ResponseTypes.instanceOf(ProjectQueryResult::class.java),
          )
      pathMatcher.match("/projects/{projectId}", destination) ->
          queryGateway.subscriptionQuery(
              ProjectQuery(ProjectId(parts.component2())),
              ResponseTypes.instanceOf(ProjectQueryResult::class.java),
              ResponseTypes.instanceOf(ProjectQueryResult::class.java),
          )
      pathMatcher.match("/projects/{projectId}/details", destination) ->
          queryGateway.subscriptionQuery(
              ProjectDetailsQuery(ProjectId(parts.component2())),
              ResponseTypes.instanceOf(ProjectDetailsQueryResult::class.java),
              ResponseTypes.instanceOf(ProjectDetailsQueryResult::class.java),
          )
      else -> null
    }
  }
}
