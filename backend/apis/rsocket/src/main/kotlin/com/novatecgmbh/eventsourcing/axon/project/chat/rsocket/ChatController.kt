package com.novatecgmbh.eventsourcing.axon.project.chat.rsocket

import com.novatecgmbh.eventsourcing.axon.application.security.RegisteredUserProfile
import com.novatecgmbh.eventsourcing.axon.project.project.api.ProjectId
import com.novatecgmbh.eventsourcing.axon.user.api.UserId
import java.time.Instant
import org.slf4j.LoggerFactory
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
  fun subscribeMessages(
      @DestinationVariable id: ProjectId,
      @AuthenticationPrincipal user: RegisteredUserProfile
  ): Flux<ChatMessage> =
      // TODO: Verify that user is participant of the project
      Flux.from(getChatRoom(id).asFlux())
          .doOnSubscribe { LOGGER.info("${user.firstname} entered chat room of project $id") }
          .doOnNext {
            LOGGER.info("${it.userName} wrote \"${it.message}\" in chat room of project $id")
          }
          .doOnCancel { LOGGER.info("${user.firstname} left chat room of project $id") }

  @MessageMapping("projects.{id}.chat.send")
  fun sendMessage(
      @DestinationVariable id: ProjectId,
      @AuthenticationPrincipal user: RegisteredUserProfile,
      message: String
  ) {
    // TODO: Verify that user is participant of the project
    getChatRoom(id).tryEmitNext(message.toChatMessage(user))
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

  companion object {
    private val LOGGER = LoggerFactory.getLogger(ChatController::class.java)
  }
}
