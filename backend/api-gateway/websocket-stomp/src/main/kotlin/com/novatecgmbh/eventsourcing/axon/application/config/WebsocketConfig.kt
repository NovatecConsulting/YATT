package com.novatecgmbh.eventsourcing.axon.application.config

import org.springframework.context.annotation.Configuration
import org.springframework.messaging.simp.config.MessageBrokerRegistry
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker
import org.springframework.web.socket.config.annotation.StompEndpointRegistry
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer

@Configuration
@EnableWebSocketMessageBroker
class WebsocketConfig : WebSocketMessageBrokerConfigurer {

  override fun configureMessageBroker(config: MessageBrokerRegistry) {
    config.enableSimpleBroker("/topic")
    config.setApplicationDestinationPrefixes("/app")
//    config.setUserDestinationPrefix("/user")
  }

  override fun registerStompEndpoints(registry: StompEndpointRegistry) {
    registry.addEndpoint("/stomp").setAllowedOrigins("http://localhost:3000")
    registry.addEndpoint("/stomp").setAllowedOrigins("http://localhost:3000").withSockJS()
  }
}
