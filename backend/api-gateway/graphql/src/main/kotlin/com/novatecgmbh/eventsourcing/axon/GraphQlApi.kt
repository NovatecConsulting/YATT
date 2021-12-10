package com.novatecgmbh.eventsourcing.axon

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication class GraphQlApi

fun main(args: Array<String>) {
  runApplication<GraphQlApi>(*args)
}
