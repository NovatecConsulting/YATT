package com.novatecgmbh.eventsourcing.axon.application.websocket

import org.axonframework.queryhandling.SubscriptionQueryResult
import org.springframework.context.annotation.Scope
import org.springframework.context.annotation.ScopedProxyMode
import org.springframework.stereotype.Component

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
