package com.novatecgmbh.eventsourcing.axon.common.command

import com.novatecgmbh.eventsourcing.axon.common.api.ExceptionStatusCode
import org.axonframework.commandhandling.CommandExecutionException
import org.axonframework.commandhandling.CommandMessage
import org.axonframework.messaging.InterceptorChain
import org.axonframework.messaging.MessageHandlerInterceptor
import org.axonframework.messaging.unitofwork.UnitOfWork
import org.axonframework.modelling.command.AggregateNotFoundException
import org.axonframework.modelling.command.ConflictingAggregateVersionException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class ExceptionWrappingHandlerInterceptor : MessageHandlerInterceptor<CommandMessage<*>?> {

  @Throws(Exception::class)
  override fun handle(
      unitOfWork: UnitOfWork<out CommandMessage<*>>,
      interceptorChain: InterceptorChain
  ): Any? =
      try {
        interceptorChain.proceed()
      } catch (e: Throwable) {
        throw CommandExecutionException(e.message, e, exceptionDetails(e))
      }

  private fun exceptionDetails(throwable: Throwable) =
      when (throwable) {
        is AggregateNotFoundException -> ExceptionStatusCode.NOT_FOUND
        is AlreadyExistsException -> ExceptionStatusCode.ALREADY_EXISTS
        is ConflictingAggregateVersionException -> ExceptionStatusCode.CONCURRENT_MODIFICATION
        is IllegalArgumentException -> ExceptionStatusCode.ILLEGAL_ARGUMENT
        else -> ExceptionStatusCode.UNKNOWN
      }.also { LOGGER.info("Mapped ${throwable::class} to status code $it") }

  companion object {
    val LOGGER: Logger = LoggerFactory.getLogger(ExceptionWrappingHandlerInterceptor::class.java)
  }
}
