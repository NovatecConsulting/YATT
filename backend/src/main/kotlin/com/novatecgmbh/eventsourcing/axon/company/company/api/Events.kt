package com.novatecgmbh.eventsourcing.axon.company.company.api

abstract class CompanyEvent(open val aggregateIdentifier: CompanyId)

data class CompanyCreatedEvent(override val aggregateIdentifier: CompanyId, val name: String) :
    CompanyEvent(aggregateIdentifier)
