package com.novatecgmbh.eventsourcing.axon.application.config

import com.novatecgmbh.eventsourcing.axon.application.auditing.AUDIT_KEYS
import com.novatecgmbh.eventsourcing.axon.application.auditing.UserInjectingCommandMessageInterceptor
import com.novatecgmbh.eventsourcing.axon.application.auditing.UserInjectingQueryMessageInterceptor
import org.axonframework.commandhandling.CommandBus
import org.axonframework.commandhandling.CommandMessage
import org.axonframework.messaging.correlation.CorrelationDataProvider
import org.axonframework.messaging.correlation.MessageOriginProvider
import org.axonframework.messaging.correlation.MultiCorrelationDataProvider
import org.axonframework.messaging.correlation.SimpleCorrelationDataProvider
import org.axonframework.queryhandling.QueryBus
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class AxonBeansEnhancementsConfiguration {

  @Autowired
  fun commandBus(commandBus: CommandBus) {
    commandBus.run { registerDispatchInterceptor(UserInjectingCommandMessageInterceptor()) }
  }

  @Autowired
  fun queryBus(queryBus: QueryBus) {
    queryBus.run { registerDispatchInterceptor(UserInjectingQueryMessageInterceptor()) }
  }
}

@Configuration
class AxonAdditionalBeansConfiguration {

  @Bean
  fun correlationDataProviders(): CorrelationDataProvider =
      MultiCorrelationDataProvider<CommandMessage<*>>(
          listOf(
              SimpleCorrelationDataProvider(*AUDIT_KEYS),
              MessageOriginProvider(),
          ))
}
