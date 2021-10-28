package com.novatecgmbh.eventsourcing.axon.application.config

import com.novatecgmbh.eventsourcing.axon.application.auditing.AUDIT_KEYS
import com.novatecgmbh.eventsourcing.axon.application.auditing.AuditingInterceptor
import com.novatecgmbh.eventsourcing.axon.common.command.ExceptionWrappingHandlerInterceptor
import java.util.concurrent.Executors
import org.axonframework.commandhandling.CommandBus
import org.axonframework.commandhandling.CommandMessage
import org.axonframework.common.AxonThreadFactory
import org.axonframework.config.EventProcessingConfigurer
import org.axonframework.eventhandling.EventMessage
import org.axonframework.eventhandling.TrackedEventMessage
import org.axonframework.eventhandling.async.SequencingPolicy
import org.axonframework.eventhandling.async.SequentialPerAggregatePolicy
import org.axonframework.messaging.StreamableMessageSource
import org.axonframework.messaging.annotation.MetaDataValue
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
      metaDataSequencingPolicy: MetaDataSequencingPolicy
  ) {
    processingConfigurer.registerDefaultSequencingPolicy { metaDataSequencingPolicy }
  }

  @Bean
  fun metaDataSequencingPolicy(): MetaDataSequencingPolicy =
      MetaDataSequencingPolicy(fallbackSequencingPolicy = SequentialPerAggregatePolicy())
}

class MetaDataSequencingPolicy(
    private val fallbackSequencingPolicy: SequencingPolicy<EventMessage<*>>
) : SequencingPolicy<EventMessage<*>> {
  override fun getSequenceIdentifierFor(event: EventMessage<*>): Any? =
      event.metaData[SEQUENCE_IDENTIFIER_META_DATA_KEY]
          ?: fallbackSequencingPolicy.getSequenceIdentifierFor(event)
}

const val SEQUENCE_IDENTIFIER_META_DATA_KEY = "sequenceIdentifier"

@MustBeDocumented
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
@MetaDataValue(SEQUENCE_IDENTIFIER_META_DATA_KEY, required = true)
annotation class SequenceIdentifier
