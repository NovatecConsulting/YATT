package com.novatecgmbh.eventsourcing.axon.company.references

import com.novatecgmbh.eventsourcing.axon.common.references.RootContextIdMapping
import com.novatecgmbh.eventsourcing.axon.common.references.RootContextIdMappingKey
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface RootContextIdMappingRepository :
    JpaRepository<RootContextIdMapping, RootContextIdMappingKey> {

  @Query(
      "select case when (count(r) > 0) then true else false end from RootContextIdMapping r " +
          "where r.key.aggregateType " +
          "= 'COMPANY' " +
          "and r.key.aggregateIdentifier = :companyId ")
  fun existsCompanyById(@Param("companyId") companyId: String): Boolean

  @Query(
      "select case when (count(r) > 0) then true else false end from RootContextIdMapping r " +
          "where r.key.aggregateType " +
          "= 'USER' " +
          "and r.key.aggregateIdentifier = :userId ")
  fun existsUserById(@Param("userId") userId: String): Boolean
}
