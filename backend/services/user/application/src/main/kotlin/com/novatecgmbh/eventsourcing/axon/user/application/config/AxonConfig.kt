package com.novatecgmbh.eventsourcing.axon.user.application.config

import com.novatecgmbh.eventsourcing.axon.application.auditing.AUDIT_KEYS
import com.novatecgmbh.eventsourcing.axon.application.auditing.SecurityContextSettingEventMessageHandlerInterceptor
import com.novatecgmbh.eventsourcing.axon.application.auditing.UserInjectingCommandMessageInterceptor
import com.novatecgmbh.eventsourcing.axon.application.auditing.UserInjectingQueryMessageInterceptor
import com.novatecgmbh.eventsourcing.axon.application.sequencing.RootContextIdentifierSequencingPolicy
import com.novatecgmbh.eventsourcing.axon.common.command.ExceptionWrappingCommandMessageHandlerInterceptor
import com.novatecgmbh.eventsourcing.axon.common.query.ExceptionWrappingQueryMessageHandlerInterceptor
import org.axonframework.commandhandling.CommandBus
import org.axonframework.commandhandling.CommandMessage
import org.axonframework.common.AxonThreadFactory
import org.axonframework.config.EventProcessingConfigurer
import org.axonframework.eventhandling.PropagatingErrorHandler
import org.axonframework.eventhandling.TrackedEventMessage
import org.axonframework.eventhandling.async.SequentialPerAggregatePolicy
import org.axonframework.messaging.StreamableMessageSource
import org.axonframework.messaging.correlation.CorrelationDataProvider
import org.axonframework.messaging.correlation.MessageOriginProvider
import org.axonframework.messaging.correlation.MultiCorrelationDataProvider
import org.axonframework.messaging.correlation.SimpleCorrelationDataProvider
import org.axonframework.queryhandling.QueryBus
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.concurrent.Executors

@Configuration
class AxonBeansEnhancementsConfiguration {

  @Autowired
  fun commandBus(commandBus: CommandBus) {
    commandBus.run {
      registerHandlerInterceptor(ExceptionWrappingCommandMessageHandlerInterceptor())
      registerDispatchInterceptor(UserInjectingCommandMessageInterceptor())
    }
  }

  @Autowired
  fun queryBus(queryBus: QueryBus) {
    queryBus.run {
      registerHandlerInterceptor(ExceptionWrappingQueryMessageHandlerInterceptor())
      registerDispatchInterceptor(UserInjectingQueryMessageInterceptor())
    }
  }

  @Autowired
  fun configureEventProcessingDefaults(processingConfigurer: EventProcessingConfigurer) {
    processingConfigurer.registerDefaultListenerInvocationErrorHandler {
      PropagatingErrorHandler.instance()
    }
  }

  @Autowired
  fun configurePooledStreamingProcessors(
      processingConfigurer: EventProcessingConfigurer,
      messageSource: StreamableMessageSource<TrackedEventMessage<*>>
  ) {
    val executorService =
        Executors.newScheduledThreadPool(8, AxonThreadFactory("event-processing-worker"))

    val psepConfig =
        EventProcessingConfigurer.PooledStreamingProcessorConfiguration { _, builder ->
          builder.workerExecutor(executorService).initialSegmentCount(16)
        }

    processingConfigurer
        .registerDefaultHandlerInterceptor { _, _ ->
          SecurityContextSettingEventMessageHandlerInterceptor()
        }
        .registerPooledStreamingEventProcessor("user-projector", { it.eventStore() }, psepConfig)
  }

  @Autowired
  fun configureSequencingPolicy(
      processingConfigurer: EventProcessingConfigurer,
      rootContextIdentifierSequencingPolicy: RootContextIdentifierSequencingPolicy
  ) {
    processingConfigurer.registerDefaultSequencingPolicy { rootContextIdentifierSequencingPolicy }
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

  @Bean
  fun rootContextIdentifierSequencingPolicy(): RootContextIdentifierSequencingPolicy =
      RootContextIdentifierSequencingPolicy(
          fallbackSequencingPolicy = SequentialPerAggregatePolicy())
}
