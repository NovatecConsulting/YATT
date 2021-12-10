package com.novatecgmbh.eventsourcing.axon.application.config

import com.novatecgmbh.eventsourcing.axon.application.security.CustomUserAuthenticationConverter
import com.novatecgmbh.eventsourcing.axon.application.security.CustomUserDetailsService
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.messaging.Message
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.simp.config.ChannelRegistration
import org.springframework.messaging.simp.stomp.StompCommand
import org.springframework.messaging.simp.stomp.StompHeaderAccessor
import org.springframework.messaging.support.ChannelInterceptor
import org.springframework.messaging.support.MessageHeaderAccessor
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.JwtDecoders
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer

@Configuration
@EnableWebSocketMessageBroker
@Order(Ordered.HIGHEST_PRECEDENCE + 99)
class WebsocketSecurityConfig(
    val userDetailsService: CustomUserDetailsService,
    @Value("\${spring.security.oauth2.resourceserver.jwt.issuer-uri}") val issuer: String
) : WebSocketMessageBrokerConfigurer {

  override fun configureClientInboundChannel(registration: ChannelRegistration) {
    val authConverter = CustomUserAuthenticationConverter(userDetailsService)
    registration.interceptors(
        object : ChannelInterceptor {
          override fun preSend(message: Message<*>, channel: MessageChannel): Message<*> {
            val accessor: StompHeaderAccessor =
                MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor::class.java)!!
            if (StompCommand.CONNECT == accessor.command) {
              val accessToken = accessor.getFirstNativeHeader("access-token")
              if (accessToken is String) {
                val jwt = JwtDecoders.fromIssuerLocation<JwtDecoder>(issuer).decode(accessToken)
                accessor.user = authConverter.convert(jwt)
              } else {
                throw RuntimeException("Authentication failed")
              }
            }
            return message
          }
        })
  }
}
