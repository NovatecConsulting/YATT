package com.novatecgmbh.eventsourcing.axon.project.chat.rsocket

import com.novatecgmbh.eventsourcing.axon.application.security.RegisteredUserProfile
import com.novatecgmbh.eventsourcing.axon.project.project.api.ProjectId
import com.novatecgmbh.eventsourcing.axon.user.api.UserId
import java.time.Instant
import org.springframework.messaging.handler.annotation.DestinationVariable
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.stereotype.Controller
import reactor.core.publisher.Flux
import reactor.core.publisher.Sinks

@Controller
class ChatController {

  private val chatrooms: MutableMap<ProjectId, Sinks.Many<ChatMessage>> = mutableMapOf()

  @MessageMapping("projects.{id}.chat")
  fun sendMessages(
      @DestinationVariable id: ProjectId,
      @AuthenticationPrincipal user: RegisteredUserProfile,
      messages: Flux<String>
  ): Flux<ChatMessage> {
    // TODO: Verify that user is participant of the project
    val chatroom = getChatRoom(id)
    messages.doOnNext { chatroom.tryEmitNext(it.toChatMessage(user)) }.subscribe()
    return Flux.from(chatroom.asFlux())
  }

  private fun getChatRoom(projectId: ProjectId): Sinks.Many<ChatMessage> =
      chatrooms.computeIfAbsent(projectId) {
        Sinks.many().multicast().onBackpressureBuffer(10, false)
      }

  private fun String.toChatMessage(user: RegisteredUserProfile) =
      ChatMessage(user.identifier, user.firstname, Instant.now(), this)

  data class ChatMessage(
      val userIdentifier: UserId,
      val userName: String,
      val timestamp: Instant,
      val message: String
  )
}
