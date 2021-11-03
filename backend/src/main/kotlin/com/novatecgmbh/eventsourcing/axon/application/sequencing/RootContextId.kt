package com.novatecgmbh.eventsourcing.axon.application.sequencing

import com.novatecgmbh.eventsourcing.axon.application.sequencing.RootContextIdentifierSequencingPolicy.Companion.ROOT_CONTEXT_IDENTIFIER_META_DATA_KEY
import org.axonframework.messaging.annotation.MetaDataValue

@MustBeDocumented
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
@MetaDataValue(ROOT_CONTEXT_IDENTIFIER_META_DATA_KEY, required = true)
annotation class RootContextId
