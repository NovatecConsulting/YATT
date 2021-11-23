package com.novatecgmbh.eventsourcing.axon.common.command

import java.lang.RuntimeException

class AlreadyExistsException : RuntimeException()

class PreconditionFailedException(message: String) : RuntimeException(message)
