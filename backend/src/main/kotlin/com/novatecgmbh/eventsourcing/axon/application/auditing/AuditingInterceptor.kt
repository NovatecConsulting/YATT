package com.novatecgmbh.eventsourcing.axon.application.auditing

import com.novatecgmbh.eventsourcing.axon.application.security.RegisteredUserPrincipal
import java.lang.RuntimeException
import java.util.function.BiFunction
import org.axonframework.commandhandling.CommandMessage
import org.axonframework.messaging.MessageDispatchInterceptor
import org.springframework.security.core.context.SecurityContextHolder

class AuditingInterceptor : MessageDispatchInterceptor<CommandMessage<*>> {
  override fun handle(
      messages: MutableList<out CommandMessage<*>>?,
  ): BiFunction<Int, CommandMessage<*>, CommandMessage<*>> {
    return BiFunction { _, message ->
      // SecurityContextHolder does not contain auth if command is dispatched in a thread other than
      // the requests thread, for example from Mono.then()
      val auth =
          SecurityContextHolder.getContext().authentication
              ?: throw RuntimeException("Authentication from security context holder is null!")
      val principal = auth.principal

      if (principal is RegisteredUserPrincipal) {
        message.andMetaData(
            mutableMapOf<String, String>(
                AUDIT_USER_ID_META_DATA_KEY to principal.identifier.toString(),
            ))
      } else {
        message
      }
    }
  }
}
