package com.novatecgmbh.eventsourcing.axon.application.config

import com.novatecgmbh.eventsourcing.axon.application.auditing.AUDIT_KEYS
import com.novatecgmbh.eventsourcing.axon.application.auditing.SecurityContextSettingEventMessageHandlerInterceptor
import com.novatecgmbh.eventsourcing.axon.application.auditing.UserInjectingCommandMessageInterceptor
import com.novatecgmbh.eventsourcing.axon.application.auditing.UserInjectingQueryMessageInterceptor
import com.novatecgmbh.eventsourcing.axon.application.sequencing.RootContextIdentifierSequencingPolicy
import com.novatecgmbh.eventsourcing.axon.common.command.ExceptionWrappingCommandMessageHandlerInterceptor
import com.novatecgmbh.eventsourcing.axon.common.query.ExceptionWrappingQueryMessageHandlerInterceptor
import java.util.concurrent.Executors
import org.axonframework.commandhandling.CommandBus
import org.axonframework.commandhandling.CommandMessage
import org.axonframework.common.AxonThreadFactory
import org.axonframework.config.EventProcessingConfigurer
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

@Configuration
class AxonConfig {

  @Autowired
  fun commandBus(
      commandBus: CommandBus,
      exceptionWrapper: ExceptionWrappingCommandMessageHandlerInterceptor
  ) {
    commandBus.run {
      registerHandlerInterceptor(exceptionWrapper)
      registerDispatchInterceptor(UserInjectingCommandMessageInterceptor())
    }
  }

  @Autowired
  fun queryBus(
      queryBus: QueryBus,
      exceptionWrapper: ExceptionWrappingQueryMessageHandlerInterceptor,
  ) {
    queryBus.run {
      registerHandlerInterceptor(exceptionWrapper)
      registerDispatchInterceptor(UserInjectingQueryMessageInterceptor())
    }
  }

  @Bean
  fun correlationDataProviders(): CorrelationDataProvider =
      MultiCorrelationDataProvider<CommandMessage<*>>(
          listOf(
              SimpleCorrelationDataProvider(*AUDIT_KEYS),
              MessageOriginProvider(),
          ))

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
        .registerPooledStreamingEventProcessor(
            "project-acl-projector", { it.eventStore() }, psepConfig)
        .registerPooledStreamingEventProcessor("project-projector", { it.eventStore() }, psepConfig)
        .registerPooledStreamingEventProcessor(
            "project-details-projector", { it.eventStore() }, psepConfig)
        .registerPooledStreamingEventProcessor("task-projector", { it.eventStore() }, psepConfig)
        .registerPooledStreamingEventProcessor(
            "participant-projector", { it.eventStore() }, psepConfig)
        .registerPooledStreamingEventProcessor("company-projector", { it.eventStore() }, psepConfig)
        .registerPooledStreamingEventProcessor(
            "employee-projector", { it.eventStore() }, psepConfig)
        .registerPooledStreamingEventProcessor("user-projector", { it.eventStore() }, psepConfig)
  }

  @Autowired
  fun configureSequencingPolicy(
      processingConfigurer: EventProcessingConfigurer,
      rootContextIdentifierSequencingPolicy: RootContextIdentifierSequencingPolicy
  ) {
    processingConfigurer.registerDefaultSequencingPolicy { rootContextIdentifierSequencingPolicy }
  }

  @Bean
  fun rootContextIdentifierSequencingPolicy(): RootContextIdentifierSequencingPolicy =
      RootContextIdentifierSequencingPolicy(
          fallbackSequencingPolicy = SequentialPerAggregatePolicy())
}
