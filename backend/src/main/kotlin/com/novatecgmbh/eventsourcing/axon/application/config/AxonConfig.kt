package com.novatecgmbh.eventsourcing.axon.application.config

import com.novatecgmbh.eventsourcing.axon.common.command.ExceptionWrappingHandlerInterceptor
import org.axonframework.commandhandling.CommandBus
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration

@Configuration
class AxonConfig {

  @Autowired
  fun commandBus(
      commandBus: CommandBus,
      exceptionWrappingHandlerInterceptor: ExceptionWrappingHandlerInterceptor,
  ) {
    commandBus.registerHandlerInterceptor(exceptionWrappingHandlerInterceptor)
  }
}
