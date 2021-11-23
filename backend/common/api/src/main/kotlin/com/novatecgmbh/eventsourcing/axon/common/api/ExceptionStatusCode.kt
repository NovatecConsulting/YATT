package com.novatecgmbh.eventsourcing.axon.common.api

enum class ExceptionStatusCode {
  ALREADY_EXISTS,
  CONCURRENT_MODIFICATION,
  ILLEGAL_ARGUMENT,
  ILLEGAL_STATE,
  NOT_FOUND,
  UNKNOWN,
  ACCESS_DENIED,
  PRECONDITION_FAILED
}
