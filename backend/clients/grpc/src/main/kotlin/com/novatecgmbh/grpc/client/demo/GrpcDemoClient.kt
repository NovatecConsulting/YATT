package com.novatecgmbh.grpc.client.demo

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication


@SpringBootApplication
@ConfigurationPropertiesScan
class GrpcDemoClient

fun main(args: Array<String>) {
  runApplication<GrpcDemoClient>(*args)
}
