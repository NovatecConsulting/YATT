package com.novatecgmbh.eventsourcing.axon.application.websocket

import com.novatecgmbh.eventsourcing.axon.application.security.RegisteredUserPrincipal
import com.novatecgmbh.eventsourcing.axon.application.security.SecurityContextHelper
import org.axonframework.queryhandling.SubscriptionQueryResult
import org.springframework.context.ApplicationListener
import org.springframework.messaging.simp.SimpMessageHeaderAccessor
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.web.socket.messaging.SessionSubscribeEvent

abstract class BaseSubscribeHandling(
    private val currentUserSubscriptions: CurrentUserSubscriptionQueries,
    private val messagingTemplate: SimpMessagingTemplate,
) : ApplicationListener<SessionSubscribeEvent> {

  abstract fun mapToSubscriptionQuery(
      destination: String,
      user: RegisteredUserPrincipal
  ): SubscriptionQueryResult<*, *>?

  override fun onApplicationEvent(event: SessionSubscribeEvent) {
    val user = getRegisteredUser(event)

    val headerAccessor: SimpMessageHeaderAccessor =
        SimpMessageHeaderAccessor.getAccessor(event.message)!! as SimpMessageHeaderAccessor
    val sessionId = headerAccessor.sessionId
    val subscriptionId = headerAccessor.subscriptionId
    val destination = headerAccessor.destination

    if (subscriptionId != null && destination != null) {
      SecurityContextHelper.setAuthentication(user.identifier.toString())
      val actualDestination = destination.replace("^/user/topic".toRegex(), "")
      val subscriptionQuery = mapToSubscriptionQuery(actualDestination, user)

      if (subscriptionQuery != null) {
        currentUserSubscriptions.add(
            subscriptionId,
            subscriptionQuery.apply {
              updates()
                  .doOnNext {
                    messagingTemplate.convertAndSendToUser(
                        user.externalUserId,
                        "/topic$actualDestination",
                        it,
                        SimpMessageHeaderAccessor.create()
                            .apply { setSessionId(sessionId) }
                            .messageHeaders,
                    )
                  }
                  .subscribe()
            })
      }
    }
  }

  private fun getRegisteredUser(event: SessionSubscribeEvent) =
      event.user.let { user ->
        if (user is UsernamePasswordAuthenticationToken) {
          val principal = user.principal
          if (principal is RegisteredUserPrincipal) {
            principal
          } else {
            throw RuntimeException("Principal is not RegisteredUserPrincipal")
          }
        } else {
          throw RuntimeException("User is not UsernamePasswordAuthenticationToken")
        }
      }
}
