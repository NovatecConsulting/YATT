package com.novatecgmbh.eventsourcing.axon

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication class GrpcApi

fun main(args: Array<String>) {
  runApplication<GrpcApi>(*args)
}
