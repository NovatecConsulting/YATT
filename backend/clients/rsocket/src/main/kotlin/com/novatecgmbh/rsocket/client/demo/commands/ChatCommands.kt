package com.novatecgmbh.rsocket.client.demo.commands

import org.slf4j.LoggerFactory
import org.springframework.shell.standard.ShellComponent
import org.springframework.shell.standard.ShellMethod
import org.springframework.shell.standard.ShellOption
import reactor.core.Disposable

@ShellComponent
class ChatCommands(val chatService: ChatService) {

  private var disposableSubscription: Disposable? = null

  private var projectIdentifier: String? = null

  @ShellMethod("Connect to project's chat room")
  fun connect(@ShellOption("-i") projectIdentifier: String): String {
    this.projectIdentifier = projectIdentifier
    disposableSubscription =
        chatService
            .subscribeMessages(projectIdentifier)
            .doOnNext { printMessage(it) }
            .doOnError { LOGGER.error("An error was raised by the responder: ${it.message}") }
            .doOnCancel { LOGGER.warn("The response flux was cancelled") }
            .doOnComplete { LOGGER.warn("The response flux was completed") }
            .subscribe()
    return "You are connected to the project's chat room"
  }

  @ShellMethod("Send message to project's chat room")
  fun send(@ShellOption("-m") message: String) {
    require(projectIdentifier != null)
    chatService.sendMessage(projectIdentifier!!, message)
  }

  @ShellMethod("Disconnect from project's chat room")
  fun disconnect(): String {
    require(disposableSubscription != null)
    disposableSubscription!!.dispose()
    return "You disconnected from the project's chat room"
  }

  private fun printMessage(message: ChatService.ChatMessage) {
    LOGGER.info("${message.userName} says: ${message.message}")
  }

  companion object {
    private val LOGGER = LoggerFactory.getLogger(ChatCommands::class.java)
  }
}
