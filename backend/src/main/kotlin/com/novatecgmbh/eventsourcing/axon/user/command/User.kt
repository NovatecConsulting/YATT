package com.novatecgmbh.eventsourcing.axon.user.command

import com.novatecgmbh.eventsourcing.axon.common.command.AlreadyExistsException
import com.novatecgmbh.eventsourcing.axon.user.api.*
import org.axonframework.commandhandling.CommandHandler
import org.axonframework.eventsourcing.EventSourcingHandler
import org.axonframework.modelling.command.AggregateCreationPolicy
import org.axonframework.modelling.command.AggregateIdentifier
import org.axonframework.modelling.command.AggregateLifecycle
import org.axonframework.modelling.command.CreationPolicy
import org.axonframework.spring.stereotype.Aggregate

@Aggregate
class User {
  @AggregateIdentifier private lateinit var aggregateIdentifier: UserId
  private lateinit var externalUserId: String
  private lateinit var firstname: String
  private lateinit var lastname: String

  @CommandHandler
  @CreationPolicy(AggregateCreationPolicy.CREATE_IF_MISSING)
  fun handle(command: RegisterUserCommand): UserId {
    if (::aggregateIdentifier.isInitialized) {
      throw AlreadyExistsException()
    }
    // TODO check for unique externalUserId
    AggregateLifecycle.apply(
        UserRegisteredEvent(
            aggregateIdentifier = command.aggregateIdentifier,
            externalUserId = command.externalUserId,
            firstname = command.firstname,
            lastname = command.lastname))
    return command.aggregateIdentifier
  }

  @CommandHandler
  fun handle(command: RenameUserCommand): UserId {
    AggregateLifecycle.apply(
        UserRenamedEvent(
            aggregateIdentifier = command.aggregateIdentifier,
            firstname = command.firstname,
            lastname = command.lastname))
    return command.aggregateIdentifier
  }

  @EventSourcingHandler
  fun on(event: UserRegisteredEvent) {
    aggregateIdentifier = event.aggregateIdentifier
    externalUserId = event.externalUserId
    firstname = event.firstname
    lastname = event.lastname
  }

  @EventSourcingHandler
  fun on(event: UserRenamedEvent) {
    firstname = event.firstname
    lastname = event.lastname
  }
}
