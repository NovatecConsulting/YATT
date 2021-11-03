package com.novatecgmbh.eventsourcing.axon.application.admin

import java.util.*
import org.axonframework.config.EventProcessingConfiguration
import org.axonframework.eventhandling.EventProcessor
import org.axonframework.eventhandling.EventTrackerStatus
import org.axonframework.eventhandling.StreamingEventProcessor
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class AxonAdministration(private val eventProcessingConfiguration: EventProcessingConfiguration) {

  fun resetStreamingEventProcessor(processingGroup: String) =
      eventProcessingConfiguration.eventProcessorByProcessingGroup(
              processingGroup, StreamingEventProcessor::class.java)
          .ifPresent {
            if (it.supportsReset()) {
              it.shutDown()
              it.resetTokens()
              it.start()
            } else {
              LOGGER.info("Processing group %s does not support reset", processingGroup)
            }
          }

  fun getStreamingEventProcessors(): List<EventProcessor> =
      eventProcessingConfiguration
          .eventProcessors()
          .values
          .filterIsInstance(StreamingEventProcessor::class.java)

  fun getEventProcessor(processingGroup: String): Optional<EventProcessor> =
      eventProcessingConfiguration.eventProcessorByProcessingGroup(processingGroup)

  // Returns a map where the key is the segment identifier, and the value is the event processing
  // status. Based on this status we can determine whether the Processor is caught up and/or is
  // replaying
  fun getStreamingEventProcessorStatus(processingGroup: String): Map<Int, EventTrackerStatus> {
    val trackingEventProcessor: Optional<StreamingEventProcessor> =
        eventProcessingConfiguration.eventProcessorByProcessingGroup(processingGroup)
    return trackingEventProcessor.map { it.processingStatus() }.orElseGet { emptyMap() }
  }

  companion object {
    private val LOGGER = LoggerFactory.getLogger(AxonAdministration::class.java)
  }
}
