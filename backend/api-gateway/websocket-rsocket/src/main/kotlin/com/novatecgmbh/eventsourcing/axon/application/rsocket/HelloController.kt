package com.novatecgmbh.eventsourcing.axon.application.rsocket

import java.time.Duration
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.stereotype.Controller
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Controller
class HelloController {
  @MessageMapping("hello")
  fun hello(): Mono<String> {
    return Mono.fromCallable { "Hello World!" }
  }

  @MessageMapping("helloStream")
  fun helloStream(): Flux<String> {
    return Flux.interval(Duration.ofSeconds(5)).map { "Hello World! $it" }
  }
}
