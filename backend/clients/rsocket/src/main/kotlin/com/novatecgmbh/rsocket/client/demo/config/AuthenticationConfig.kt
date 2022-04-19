package com.novatecgmbh.rsocket.client.demo.config

import java.net.URI
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.security.rsocket.metadata.BearerTokenMetadata
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.RestTemplate

@Configuration
class AuthenticationConfiguration {

  @Bean fun restTemplate(restTemplateBuilder: RestTemplateBuilder) = RestTemplateBuilder().build()

  @Bean
  fun bearerTokenMetadata(
      restTemplate: RestTemplate,
      properties: AuthenticationProperties
  ): BearerTokenMetadata {
    val map =
        LinkedMultiValueMap<String, String>().apply {
          add("client_id", "my-backend")
          add("username", properties.username)
          add("password", properties.password)
          add("grant_type", "password")
        }
    return HttpHeaders()
        .apply { contentType = MediaType.APPLICATION_FORM_URLENCODED }
        .let { HttpEntity(map, it) }
        .let { restTemplate.postForEntity(properties.url, it, AuthResponse::class.java) }
        .let { BearerTokenMetadata(it.body!!.access_token) }
  }

  private class AuthResponse(var access_token: String)
}

@ConstructorBinding
@ConfigurationProperties(prefix = "custom.auth")
data class AuthenticationProperties(val url: URI, val username: String, val password: String)
