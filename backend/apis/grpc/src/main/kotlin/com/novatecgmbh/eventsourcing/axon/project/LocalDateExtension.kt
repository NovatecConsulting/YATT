package com.novatecgmbh.eventsourcing.axon.project

import com.google.protobuf.Timestamp
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime.MIDNIGHT
import java.time.ZoneOffset.UTC

fun LocalDate.toTimestamp() =
    Timestamp.newBuilder().setSeconds(this.toEpochSecond(MIDNIGHT, UTC)).build()

fun Timestamp.toLocalDate() = LocalDate.ofInstant(Instant.ofEpochSecond(this.seconds), UTC)
