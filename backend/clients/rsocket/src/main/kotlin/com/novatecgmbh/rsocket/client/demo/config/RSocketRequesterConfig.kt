package com.novatecgmbh.rsocket.client.demo.config

import io.rsocket.metadata.WellKnownMimeType
import java.net.URI
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.boot.rsocket.messaging.RSocketStrategiesCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.messaging.rsocket.RSocketRequester
import org.springframework.messaging.rsocket.RSocketStrategies
import org.springframework.security.rsocket.metadata.BearerTokenAuthenticationEncoder
import org.springframework.security.rsocket.metadata.BearerTokenMetadata
import org.springframework.util.MimeType
import org.springframework.util.MimeTypeUtils

@Configuration
class RSocketRequesterConfiguration(private val properties: CustomRSocketProperties) {

  private val authenticationMimeType: MimeType =
      MimeTypeUtils.parseMimeType(WellKnownMimeType.MESSAGE_RSOCKET_AUTHENTICATION.string)

  @Bean
  fun bearerTokenRSocketStrategyCustomizer(): RSocketStrategiesCustomizer =
      RSocketStrategiesCustomizer { strategy: RSocketStrategies.Builder ->
    strategy.encoder(BearerTokenAuthenticationEncoder())
  }

  @Bean
  fun rsocketRequester(
      rsocketRequesterBuilder: RSocketRequester.Builder,
      token: BearerTokenMetadata
  ) = rsocketRequesterBuilder.setupMetadata(token, authenticationMimeType).websocket(properties.uri)
}

@ConstructorBinding
@ConfigurationProperties(prefix = "custom.rsocket.server")
data class CustomRSocketProperties(val uri: URI)
