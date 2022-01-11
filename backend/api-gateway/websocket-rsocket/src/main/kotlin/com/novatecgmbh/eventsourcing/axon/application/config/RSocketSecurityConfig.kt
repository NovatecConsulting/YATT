package com.novatecgmbh.eventsourcing.axon.application.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.messaging.rsocket.RSocketStrategies
import org.springframework.messaging.rsocket.annotation.support.RSocketMessageHandler
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity
import org.springframework.security.config.annotation.rsocket.EnableRSocketSecurity
import org.springframework.security.config.annotation.rsocket.RSocketSecurity
import org.springframework.security.messaging.handler.invocation.reactive.AuthenticationPrincipalArgumentResolver
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoders
import org.springframework.security.oauth2.server.resource.authentication.JwtReactiveAuthenticationManager
import org.springframework.security.rsocket.core.PayloadSocketAcceptorInterceptor

@Configuration
@EnableRSocketSecurity
@EnableReactiveMethodSecurity
class RSocketSecurityConfig(
    @Value("\${spring.security.oauth2.resourceserver.jwt.issuer-uri}") val issuer: String,
) {

  @Bean
  fun messageHandler(strategies: RSocketStrategies) =
      RSocketMessageHandler().apply {
        argumentResolverConfigurer.addCustomResolver(AuthenticationPrincipalArgumentResolver())
        rSocketStrategies = strategies
      }

  @Bean
  fun rsocketInterceptor(
      rsocket: RSocketSecurity,
      decoder: ReactiveJwtDecoder,
      converter: UserProfileAuthenticationConverter
  ): PayloadSocketAcceptorInterceptor =
      rsocket
          .authorizePayload { authorize -> authorize.anyExchange().authenticated() }
          .jwt {
            val manager = JwtReactiveAuthenticationManager(decoder)
            manager.setJwtAuthenticationConverter(converter)
            it.authenticationManager(manager)
          }
          .build()
}
