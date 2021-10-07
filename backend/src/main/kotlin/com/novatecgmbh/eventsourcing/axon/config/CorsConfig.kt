package com.novatecgmbh.eventsourcing.axon.config

import java.util.*
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import org.springframework.web.filter.CorsFilter

@Configuration
class CorsConfig {
  @Bean
  fun corsFilter(@Value("\${app.cors.allowed-origins}") allowedOrigins: List<String>?) =
      CorsFilter(
          UrlBasedCorsConfigurationSource().apply {
            this.registerCorsConfiguration(
                "/**",
                CorsConfiguration().apply {
                  this.allowCredentials = true
                  this.allowedOrigins = allowedOrigins
                  this.allowedMethods = Collections.singletonList("*")
                  this.allowedHeaders = Collections.singletonList("*")
                })
          })
}
