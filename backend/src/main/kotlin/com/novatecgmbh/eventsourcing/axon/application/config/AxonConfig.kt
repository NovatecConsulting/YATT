package com.novatecgmbh.eventsourcing.axon.application.config

import com.novatecgmbh.eventsourcing.axon.application.auditing.AUDIT_KEYS
import com.novatecgmbh.eventsourcing.axon.application.auditing.AuditingInterceptor
import com.novatecgmbh.eventsourcing.axon.common.command.ExceptionWrappingHandlerInterceptor
import org.axonframework.commandhandling.CommandBus
import org.axonframework.commandhandling.CommandMessage
import org.axonframework.messaging.correlation.CorrelationDataProvider
import org.axonframework.messaging.correlation.MessageOriginProvider
import org.axonframework.messaging.correlation.MultiCorrelationDataProvider
import org.axonframework.messaging.correlation.SimpleCorrelationDataProvider
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class AxonConfig {

  @Autowired
  fun commandBus(
      commandBus: CommandBus,
      exceptionWrappingHandlerInterceptor: ExceptionWrappingHandlerInterceptor,
  ) {
    commandBus.run {
      registerHandlerInterceptor(exceptionWrappingHandlerInterceptor)
      registerDispatchInterceptor(AuditingInterceptor())
    }
  }

  @Bean
  fun correlationDataProviders(): CorrelationDataProvider =
      MultiCorrelationDataProvider<CommandMessage<*>>(
          listOf(
              SimpleCorrelationDataProvider(*AUDIT_KEYS),
              MessageOriginProvider(),
          ))
}
