package com.novatecgmbh.eventsourcing.axon.application.auditing

import org.axonframework.messaging.annotation.MetaDataValue

@MustBeDocumented
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
@MetaDataValue(AUDIT_USER_ID_META_DATA_KEY, required = true)
annotation class AuditUserId