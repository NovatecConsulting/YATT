package com.novatecgmbh.eventsourcing.axon.common.command

import com.novatecgmbh.eventsourcing.axon.application.config.MetaDataSequencingPolicy
import org.axonframework.messaging.MetaData
import org.axonframework.modelling.command.AggregateLifecycle

abstract class BaseAggregate {
  abstract fun getSequenceIdentifier(): String

  protected fun apply(
      payload: Any,
      metaData: MetaData = MetaData(mutableMapOf<String, Any>()),
      sequenceIdentifier: String = getSequenceIdentifier()
  ) {
    AggregateLifecycle.apply(
        payload,
        metaData.mergedWith(
            mutableMapOf(MetaDataSequencingPolicy.META_DATA_KEY to sequenceIdentifier)))
  }
}