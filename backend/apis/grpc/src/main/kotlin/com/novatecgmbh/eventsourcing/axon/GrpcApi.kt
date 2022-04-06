package com.novatecgmbh.eventsourcing.axon

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication

@SpringBootApplication
class GrpcApi

fun main(args: Array<String>) {
  runApplication<GrpcApi>(*args)
}
