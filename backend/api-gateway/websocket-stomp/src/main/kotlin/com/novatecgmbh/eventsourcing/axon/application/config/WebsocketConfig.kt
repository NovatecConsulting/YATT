package com.novatecgmbh.eventsourcing.axon.application.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.messaging.simp.config.MessageBrokerRegistry
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker
import org.springframework.web.socket.config.annotation.StompEndpointRegistry
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer

@Configuration
@EnableWebSocketMessageBroker
class WebsocketConfig(@Value("\${app.cors.allowed-origins}") val allowedOrigins: List<String>?) :
    WebSocketMessageBrokerConfigurer {

  override fun configureMessageBroker(config: MessageBrokerRegistry) {
    config.enableSimpleBroker("/topic")
    config.setApplicationDestinationPrefixes("/app")
  }

  override fun registerStompEndpoints(registry: StompEndpointRegistry) {
    registry
        .addEndpoint("/stomp")
        .setAllowedOrigins(*allowedOrigins?.toTypedArray() ?: emptyArray())
    registry
        .addEndpoint("/stomp")
        .setAllowedOrigins(*allowedOrigins?.toTypedArray() ?: emptyArray())
        .withSockJS()
  }
}
