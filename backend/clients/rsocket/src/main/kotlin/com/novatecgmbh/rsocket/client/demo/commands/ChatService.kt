package com.novatecgmbh.rsocket.client.demo.commands

import java.time.Instant
import org.springframework.messaging.rsocket.RSocketRequester
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux

@Service
class ChatService(private val rsocketRequester: RSocketRequester) {

  fun subscribeMessages(projectIdentifier: String): Flux<ChatMessage> =
      rsocketRequester
          .route("projects.{id}.chat", projectIdentifier)
          .retrieveFlux(ChatMessage::class.java)

  fun sendMessage(projectIdentifier: String, message: String) {
    rsocketRequester
        .route("projects.{id}.chat.send", projectIdentifier)
        .data(message)
        .send()
        .block()
  }

  data class ChatMessage(
      var userIdentifier: String,
      var userName: String,
      var timestamp: Instant,
      var message: String
  )
}
