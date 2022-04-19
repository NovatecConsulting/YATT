package com.novatecgmbh.rsocket.client.demo

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.security.rsocket.RSocketSecurityAutoConfiguration
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@SpringBootApplication(exclude = [RSocketSecurityAutoConfiguration::class])
@ConfigurationPropertiesScan
class RSocketDemoClient

fun main(args: Array<String>) {
  runApplication<RSocketDemoClient>(*args)
}
