package com.novatecgmbh.eventsourcing.axon.company.company.api

import org.axonframework.modelling.command.TargetAggregateIdentifier

abstract class CompanyCommand(
    @TargetAggregateIdentifier open val aggregateIdentifier: CompanyId,
)

data class CreateCompanyCommand(
    @TargetAggregateIdentifier override val aggregateIdentifier: CompanyId,
    val name: String
) : CompanyCommand(aggregateIdentifier)
