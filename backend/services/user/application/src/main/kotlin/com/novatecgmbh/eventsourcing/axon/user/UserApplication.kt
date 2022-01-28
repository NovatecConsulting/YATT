package com.novatecgmbh.eventsourcing.axon.user

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication class UserApplication

fun main(args: Array<String>) {
  runApplication<UserApplication>(*args)
}
