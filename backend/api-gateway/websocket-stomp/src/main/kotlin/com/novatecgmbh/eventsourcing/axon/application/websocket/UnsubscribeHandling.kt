package com.novatecgmbh.eventsourcing.axon.application.websocket

import org.springframework.context.ApplicationListener
import org.springframework.messaging.simp.SimpMessageHeaderAccessor
import org.springframework.stereotype.Component
import org.springframework.web.socket.messaging.SessionDisconnectEvent
import org.springframework.web.socket.messaging.SessionUnsubscribeEvent

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
class DisconnectHandling(val currentUserSubscriptions: CurrentUserSubscriptionQueries) :
    ApplicationListener<SessionDisconnectEvent> {

  override fun onApplicationEvent(event: SessionDisconnectEvent) {
    currentUserSubscriptions.removeAll()
  }
}
