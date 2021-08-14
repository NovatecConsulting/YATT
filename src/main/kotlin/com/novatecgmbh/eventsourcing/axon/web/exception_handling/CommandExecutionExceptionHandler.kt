package com.novatecgmbh.eventsourcing.axon.web.exception_handling

import com.novatecgmbh.eventsourcing.axon.coreapi.ExceptionStatusCode
import org.axonframework.commandhandling.CommandExecutionException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler

@ControllerAdvice
class CommandExecutionExceptionHandler : ResponseEntityExceptionHandler() {

  @ExceptionHandler(value = [CommandExecutionException::class])
  fun handle(ex: CommandExecutionException, request: WebRequest): ResponseEntity<Any> {
    val details = ex.getDetails<ExceptionStatusCode>()
    return when {
      details.isPresent ->
          when (details.get()) {
            ExceptionStatusCode.CONCURRENT_MODIFICATION -> ResponseEntity(HttpStatus.CONFLICT)
            ExceptionStatusCode.ILLEGAL_ARGUMENT -> ResponseEntity(HttpStatus.BAD_REQUEST)
            ExceptionStatusCode.ALREADY_EXISTS -> ResponseEntity(HttpStatus.CONFLICT)
          }
      ex.message?.contains("not found") == true -> ResponseEntity(HttpStatus.NOT_FOUND)
      else -> ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR)
    }
  }
}
