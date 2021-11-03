package com.novatecgmbh.eventsourcing.axon.application.sequencing

import org.axonframework.eventhandling.EventMessage
import org.axonframework.eventhandling.async.SequencingPolicy

class RootContextIdentifierSequencingPolicy(
    private val fallbackSequencingPolicy: SequencingPolicy<EventMessage<*>>
) : SequencingPolicy<EventMessage<*>> {
  override fun getSequenceIdentifierFor(event: EventMessage<*>): Any? =
      event.metaData[ROOT_CONTEXT_IDENTIFIER_META_DATA_KEY]
          ?: fallbackSequencingPolicy.getSequenceIdentifierFor(event)

  companion object {
    const val ROOT_CONTEXT_IDENTIFIER_META_DATA_KEY = "sequenceIdentifier"
  }
}
