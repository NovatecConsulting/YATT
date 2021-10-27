package com.novatecgmbh.eventsourcing.axon.company.company.command

import com.novatecgmbh.eventsourcing.axon.common.command.AlreadyExistsException
import com.novatecgmbh.eventsourcing.axon.common.command.BaseAggregate
import com.novatecgmbh.eventsourcing.axon.company.company.api.CompanyCreatedEvent
import com.novatecgmbh.eventsourcing.axon.company.company.api.CompanyId
import com.novatecgmbh.eventsourcing.axon.company.company.api.CreateCompanyCommand
import org.axonframework.commandhandling.CommandHandler
import org.axonframework.eventsourcing.EventSourcingHandler
import org.axonframework.modelling.command.AggregateCreationPolicy
import org.axonframework.modelling.command.AggregateIdentifier
import org.axonframework.modelling.command.CreationPolicy
import org.axonframework.spring.stereotype.Aggregate

@Aggregate
class Company : BaseAggregate() {
  @AggregateIdentifier private lateinit var aggregateIdentifier: CompanyId
  private lateinit var name: String

  @CommandHandler
  @CreationPolicy(AggregateCreationPolicy.CREATE_IF_MISSING)
  fun handle(command: CreateCompanyCommand): CompanyId {
    if (::aggregateIdentifier.isInitialized) {
      throw AlreadyExistsException()
    }
    apply(
        CompanyCreatedEvent(aggregateIdentifier = command.aggregateIdentifier, name = command.name),
        sequenceIdentifier = command.aggregateIdentifier.identifier)
    return command.aggregateIdentifier
  }

  @EventSourcingHandler
  fun on(event: CompanyCreatedEvent) {
    aggregateIdentifier = event.aggregateIdentifier
    name = event.name
  }

  override fun getSequenceIdentifier() = aggregateIdentifier.identifier
}
