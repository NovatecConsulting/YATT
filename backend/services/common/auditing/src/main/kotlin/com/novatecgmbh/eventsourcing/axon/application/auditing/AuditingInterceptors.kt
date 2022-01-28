package com.novatecgmbh.eventsourcing.axon.application.auditing

import com.novatecgmbh.eventsourcing.axon.application.security.SecurityContextHelper.setAuthentication
import com.novatecgmbh.eventsourcing.axon.application.security.RegisteredUserPrincipal
import java.util.function.BiFunction
import org.axonframework.commandhandling.CommandMessage
import org.axonframework.eventhandling.EventMessage
import org.axonframework.messaging.InterceptorChain
import org.axonframework.messaging.MessageDispatchInterceptor
import org.axonframework.messaging.MessageHandlerInterceptor
import org.axonframework.messaging.unitofwork.UnitOfWork
import org.axonframework.queryhandling.QueryMessage
import org.springframework.security.core.context.SecurityContextHolder

class UserInjectingCommandMessageInterceptor : MessageDispatchInterceptor<CommandMessage<*>> {
  override fun handle(
      messages: MutableList<out CommandMessage<*>>?,
  ): BiFunction<Int, CommandMessage<*>, CommandMessage<*>> = BiFunction { _, message ->
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

class UserInjectingQueryMessageInterceptor : MessageDispatchInterceptor<QueryMessage<*, *>> {
  override fun handle(
      messages: MutableList<out QueryMessage<*, *>>?,
  ): BiFunction<Int, QueryMessage<*, *>, QueryMessage<*, *>> = BiFunction { _, message ->
    // SecurityContextHolder does not contain auth if query is dispatched in a thread other than
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

class SecurityContextSettingEventMessageHandlerInterceptor :
    MessageHandlerInterceptor<EventMessage<*>?> {
  override fun handle(
      unitOfWork: UnitOfWork<out EventMessage<*>>,
      interceptorChain: InterceptorChain
  ) {
    setAuthentication(unitOfWork.message.metaData[AUDIT_USER_ID_META_DATA_KEY].toString())
    interceptorChain.proceed()
  }
}

const val AUDIT_USER_ID_META_DATA_KEY = "auditUserId"

val AUDIT_KEYS = arrayOf(AUDIT_USER_ID_META_DATA_KEY)
