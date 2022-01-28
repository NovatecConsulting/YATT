package com.novatecgmbh.eventsourcing.axon.company.company.query

import com.novatecgmbh.eventsourcing.axon.company.company.api.CompanyId
import com.novatecgmbh.eventsourcing.axon.company.company.api.CompanyQueryResult
import javax.persistence.Column
import javax.persistence.EmbeddedId
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "company")
class CompanyProjection(
    @EmbeddedId var identifier: CompanyId,
    @Column(nullable = false) var version: Long,
    @Column(nullable = false) var name: String
) {
  fun toQueryResult() = CompanyQueryResult(identifier, version, name)
}
