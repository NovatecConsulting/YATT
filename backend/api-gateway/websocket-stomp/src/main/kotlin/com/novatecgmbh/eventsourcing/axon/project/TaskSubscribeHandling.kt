package com.novatecgmbh.eventsourcing.axon.project

import com.novatecgmbh.eventsourcing.axon.application.security.RegisteredUserPrincipal
import com.novatecgmbh.eventsourcing.axon.application.websocket.BaseSubscribeHandling
import com.novatecgmbh.eventsourcing.axon.application.websocket.CurrentUserSubscriptionQueries
import com.novatecgmbh.eventsourcing.axon.project.project.api.*
import com.novatecgmbh.eventsourcing.axon.project.task.api.TaskId
import com.novatecgmbh.eventsourcing.axon.project.task.api.TaskQuery
import com.novatecgmbh.eventsourcing.axon.project.task.api.TaskQueryResult
import com.novatecgmbh.eventsourcing.axon.project.task.api.TasksByProjectQuery
import org.axonframework.messaging.responsetypes.ResponseTypes
import org.axonframework.queryhandling.QueryGateway
import org.axonframework.queryhandling.SubscriptionQueryResult
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Component
import org.springframework.util.AntPathMatcher

@Component
class TaskSubscribeHandling(
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
      pathMatcher.match("/projects/{projectId}/tasks", destination) ->
          queryGateway.subscriptionQuery(
              TasksByProjectQuery(ProjectId(parts.component2())),
              ResponseTypes.multipleInstancesOf(TaskQueryResult::class.java),
              ResponseTypes.instanceOf(TaskQueryResult::class.java),
          )
      pathMatcher.match("/tasks/{taskId}", destination) ->
          queryGateway.subscriptionQuery(
              TaskQuery(TaskId(parts.component2())),
              ResponseTypes.instanceOf(TaskQueryResult::class.java),
              ResponseTypes.instanceOf(TaskQueryResult::class.java),
          )
      else -> null
    }
  }
}
