package com.novatecgmbh.eventsourcing.axon.common.query

import com.novatecgmbh.eventsourcing.axon.common.api.ExceptionStatusCode
import org.axonframework.messaging.InterceptorChain
import org.axonframework.messaging.MessageHandlerInterceptor
import org.axonframework.messaging.unitofwork.UnitOfWork
import org.axonframework.queryhandling.QueryExecutionException
import org.axonframework.queryhandling.QueryMessage
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class ExceptionWrappingQueryMessageHandlerInterceptor :
    MessageHandlerInterceptor<QueryMessage<*, *>?> {

  @Throws(Exception::class)
  override fun handle(
      unitOfWork: UnitOfWork<out QueryMessage<*, *>>,
      interceptorChain: InterceptorChain
  ): Any? =
      try {
        interceptorChain.proceed()
      } catch (e: Throwable) {
        throw QueryExecutionException(e.message, e, exceptionDetails(e))
      }

  private fun exceptionDetails(throwable: Throwable) =
      when (throwable) {
        is IllegalAccessException -> ExceptionStatusCode.ACCESS_DENIED
        else -> ExceptionStatusCode.UNKNOWN
      }.also { LOGGER.info("Mapped ${throwable::class} to status code $it") }

  companion object {
    val LOGGER: Logger =
        LoggerFactory.getLogger(ExceptionWrappingQueryMessageHandlerInterceptor::class.java)
  }
}
