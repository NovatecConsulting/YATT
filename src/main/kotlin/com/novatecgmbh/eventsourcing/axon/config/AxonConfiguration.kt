package com.novatecgmbh.eventsourcing.axon.config

import com.novatecgmbh.eventsourcing.axon.common.command.ExceptionWrappingHandlerInterceptor
import org.axonframework.commandhandling.CommandBus
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration

@Configuration
class AxonConfiguration {

  @Autowired
  fun commandBus(
      commandBus: CommandBus,
      exceptionWrappingHandlerInterceptor: ExceptionWrappingHandlerInterceptor,
  ) {
    commandBus.registerHandlerInterceptor(exceptionWrappingHandlerInterceptor)
  }
}
