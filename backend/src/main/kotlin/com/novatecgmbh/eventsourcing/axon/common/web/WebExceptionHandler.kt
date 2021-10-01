package com.novatecgmbh.eventsourcing.axon.common.web

import com.novatecgmbh.eventsourcing.axon.common.api.ExceptionStatusCode
import com.novatecgmbh.eventsourcing.axon.common.api.ExceptionStatusCode.*
import com.novatecgmbh.eventsourcing.axon.common.api.ExceptionStatusCode.NOT_FOUND
import org.axonframework.commandhandling.CommandExecutionException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.*
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler

@ControllerAdvice
class WebExceptionHandler : ResponseEntityExceptionHandler() {

  @ExceptionHandler(value = [CommandExecutionException::class])
  fun handle(ex: CommandExecutionException, request: WebRequest): ResponseEntity<Any> =
      ex.getDetails<ExceptionStatusCode>().let {
        LOGGER.info(ex.message)
        when {
          it.isPresent ->
              when (it.get()) {
                ALREADY_EXISTS -> ResponseEntity(CONFLICT)
                CONCURRENT_MODIFICATION -> ResponseEntity(CONFLICT)
                ILLEGAL_ARGUMENT -> ResponseEntity(BAD_REQUEST)
                ILLEGAL_STATE -> ResponseEntity(CONFLICT)
                NOT_FOUND -> ResponseEntity(HttpStatus.NOT_FOUND)
                UNKNOWN -> ResponseEntity(INTERNAL_SERVER_ERROR)
              }
          else -> ResponseEntity(INTERNAL_SERVER_ERROR)
        }
      }

  companion object {
    val LOGGER: Logger = LoggerFactory.getLogger(WebExceptionHandler::class.java)
  }
}
