package com.novatecgmbh.eventsourcing.axon.user.user.command

// import com.novatecgmbh.eventsourcing.axon.application.auditing.AUDIT_USER_ID_META_DATA_KEY
import com.novatecgmbh.eventsourcing.axon.common.command.AlreadyExistsException
import com.novatecgmbh.eventsourcing.axon.common.command.BaseAggregate
import com.novatecgmbh.eventsourcing.axon.user.api.*
import com.novatecgmbh.eventsourcing.axon.user.user.command.view.UserUniqueKeyRepository
import org.axonframework.commandhandling.CommandHandler
import org.axonframework.eventsourcing.EventSourcingHandler
import org.axonframework.messaging.MetaData
import org.axonframework.modelling.command.AggregateCreationPolicy
import org.axonframework.modelling.command.AggregateIdentifier
import org.axonframework.modelling.command.AggregateLifecycle
import org.axonframework.modelling.command.CreationPolicy
import org.axonframework.spring.stereotype.Aggregate
import org.springframework.beans.factory.annotation.Autowired

@Aggregate
class User : BaseAggregate() {
  @AggregateIdentifier private lateinit var aggregateIdentifier: UserId
  private lateinit var externalUserId: String
  private lateinit var firstname: String
  private lateinit var lastname: String

  @CommandHandler
  @CreationPolicy(AggregateCreationPolicy.CREATE_IF_MISSING)
  fun handle(
      command: RegisterUserCommand,
      @Autowired userUniqueKeyRepository: UserUniqueKeyRepository
  ): UserId {
    if (::aggregateIdentifier.isInitialized) {
      throw AlreadyExistsException()
    }
    assertNoUserExistsForExternalIdentifier(userUniqueKeyRepository, command.externalUserId)
    apply(
        UserRegisteredEvent(
            aggregateIdentifier = command.aggregateIdentifier,
            externalUserId = command.externalUserId,
            firstname = command.firstname,
            lastname = command.lastname),
        MetaData(
            //            mutableMapOf(AUDIT_USER_ID_META_DATA_KEY to
            // command.aggregateIdentifier.identifier)),
            mutableMapOf(
                "auditUserId" to command.aggregateIdentifier.identifier)), // TODO constant?
        rootContextId = command.aggregateIdentifier.identifier)
    return command.aggregateIdentifier
  }

  private fun assertNoUserExistsForExternalIdentifier(
      userUniqueKeyRepository: UserUniqueKeyRepository,
      externalUserId: String
  ) {
    if (userUniqueKeyRepository.existsByExternalUserId(externalUserId)) {
      throw IllegalArgumentException("A user already exists for this external identifier")
    }
  }

  @CommandHandler
  fun handle(command: RenameUserCommand): Long {
    apply(
        UserRenamedEvent(
            aggregateIdentifier = command.aggregateIdentifier,
            firstname = command.firstname,
            lastname = command.lastname))
    return AggregateLifecycle.getVersion()
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

  override fun getRootContextId() = aggregateIdentifier.identifier
}
