package com.novatecgmbh.eventsourcing.axon.project.project.websocket

import com.novatecgmbh.eventsourcing.axon.application.security.RegisteredUserPrincipal
import com.novatecgmbh.eventsourcing.axon.application.security.SecurityContextHelper
import com.novatecgmbh.eventsourcing.axon.project.project.api.*
import com.novatecgmbh.eventsourcing.axon.project.task.api.TaskQueryResult
import com.novatecgmbh.eventsourcing.axon.project.task.api.TasksByProjectQuery
import org.axonframework.messaging.responsetypes.ResponseTypes
import org.axonframework.queryhandling.QueryGateway
import org.axonframework.queryhandling.SubscriptionQueryResult
import org.springframework.context.ApplicationListener
import org.springframework.context.annotation.Scope
import org.springframework.context.annotation.ScopedProxyMode
import org.springframework.messaging.simp.SimpMessageHeaderAccessor
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.messaging.simp.annotation.SendToUser
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.stereotype.Component
import org.springframework.web.socket.messaging.SessionSubscribeEvent
import org.springframework.web.socket.messaging.SessionUnsubscribeEvent

@Component
class SubscribeHandling(
    val currentUserSubscriptions: CurrentUserSubscriptionQueries,
    val queryGateway: QueryGateway,
    val messagingTemplate: SimpMessagingTemplate,
) : ApplicationListener<SessionSubscribeEvent> {

  @SendToUser
  override fun onApplicationEvent(event: SessionSubscribeEvent) {
    val user = event.user
    val userId =
        if (user is UsernamePasswordAuthenticationToken) {
          val principal = user.principal
          if (principal is RegisteredUserPrincipal) {
            principal.identifier
          } else {
            // TODO
            throw RuntimeException("principal is not RegisteredUserPrincipal")
          }
        } else {
          // TODO
          throw RuntimeException("user is not UsernamePasswordAuthenticationToken")
        }

    val headerAccessor: SimpMessageHeaderAccessor =
        SimpMessageHeaderAccessor.getAccessor(event.message)!! as SimpMessageHeaderAccessor
    val sessionId = headerAccessor.sessionId
    val subscriptionId = headerAccessor.subscriptionId
    val destination = headerAccessor.destination

    if (subscriptionId != null && destination != null) {
      SecurityContextHelper.setAuthentication(userId.identifier)
      val paths = destination.split("/").drop(3) // drop empty string, user, topic

      val subscriptionQuery: SubscriptionQueryResult<*, *>? =
          when (paths.getOrNull(0)) {
            "projects" ->
                when (paths.getOrNull(1)) {
                  null ->
                      queryGateway.subscriptionQuery(
                          MyProjectsQuery(userId),
                          ResponseTypes.multipleInstancesOf(ProjectQueryResult::class.java),
                          ResponseTypes.instanceOf(ProjectQueryResult::class.java),
                      )
                  else ->
                      when (paths.getOrNull(2)) {
                        null ->
                            queryGateway.subscriptionQuery(
                                ProjectQuery(ProjectId(paths[1])),
                                ResponseTypes.instanceOf(ProjectQueryResult::class.java),
                                ResponseTypes.instanceOf(ProjectQueryResult::class.java),
                            )
                        "tasks" ->
                            queryGateway.subscriptionQuery(
                                TasksByProjectQuery(ProjectId(paths[1])),
                                ResponseTypes.multipleInstancesOf(TaskQueryResult::class.java),
                                ResponseTypes.instanceOf(TaskQueryResult::class.java),
                            )
                        "details" ->
                            queryGateway.subscriptionQuery(
                                ProjectDetailsQuery(ProjectId(paths[1])),
                                ResponseTypes.instanceOf(ProjectQueryResult::class.java),
                                ResponseTypes.instanceOf(ProjectQueryResult::class.java),
                            )
                        else -> null
                      }
                }
            else -> null
          }

      if (subscriptionQuery != null) {
        currentUserSubscriptions.add(
            subscriptionId,
            subscriptionQuery.apply {
              updates()
                  .doOnNext {
                    messagingTemplate.convertAndSendToUser(
                        user.name,
                        "/topic/${paths.joinToString("/")}",
                        it,
                        SimpMessageHeaderAccessor.create()
                            .apply { setSessionId(sessionId) }
                            .messageHeaders,
                    )
                  }
                  .subscribe()
            })
      } else {
        println("not found")
        // TODO error handling not found
      }
    }
  }
}

@Component
class UnsubscribeHandling(val currentUserSubscriptions: CurrentUserSubscriptionQueries) :
    ApplicationListener<SessionUnsubscribeEvent> {

  override fun onApplicationEvent(event: SessionUnsubscribeEvent) {
    val headerAccessor: SimpMessageHeaderAccessor =
        SimpMessageHeaderAccessor.getAccessor(event.message)!! as SimpMessageHeaderAccessor
    val subscriptionId = headerAccessor.subscriptionId

    if (subscriptionId != null) {
      currentUserSubscriptions.remove(subscriptionId)
    }
  }
}

@Component
@Scope(scopeName = "websocket", proxyMode = ScopedProxyMode.TARGET_CLASS)
class CurrentUserSubscriptionQueries {
  private val subscriptions = mutableMapOf<String, SubscriptionQueryResult<*, *>>()

  fun add(subscriptionId: String, subscriptionQuery: SubscriptionQueryResult<*, *>) {
    val oldSubscription = subscriptions[subscriptionId]
    subscriptions[subscriptionId] = subscriptionQuery
    oldSubscription?.cancel()
  }

  fun remove(subscriptionId: String) {
    subscriptions.remove(subscriptionId)?.cancel()
  }
}
