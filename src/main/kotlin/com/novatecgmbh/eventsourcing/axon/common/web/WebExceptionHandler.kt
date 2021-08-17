package com.novatecgmbh.eventsourcing.axon.common.web

import com.novatecgmbh.eventsourcing.axon.common.api.ExceptionStatusCode
import org.axonframework.commandhandling.CommandExecutionException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
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
                ExceptionStatusCode.ALREADY_EXISTS -> ResponseEntity(HttpStatus.CONFLICT)
                ExceptionStatusCode.CONCURRENT_MODIFICATION -> ResponseEntity(HttpStatus.CONFLICT)
                ExceptionStatusCode.ILLEGAL_ARGUMENT -> ResponseEntity(HttpStatus.BAD_REQUEST)
                ExceptionStatusCode.NOT_FOUND -> ResponseEntity(HttpStatus.NOT_FOUND)
                ExceptionStatusCode.UNKNOWN -> ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR)
              }
          else -> ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR)
        }
      }

  companion object {
    val LOGGER: Logger = LoggerFactory.getLogger(WebExceptionHandler::class.java)
  }
}
