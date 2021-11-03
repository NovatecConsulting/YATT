package com.novatecgmbh.eventsourcing.axon.application.references

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface RootContextIdMappingRepository :
    JpaRepository<RootContextIdMapping, RootContextIdMappingKey> {

  @Query(
      "select r.key.rootContextId from RootContextIdMapping r " +
          "where r.key.aggregateType " +
          "=  'TASK' " +
          "and r.key.aggregateIdentifier = :taskId ")
  fun findProjectIdByTaskId(@Param("taskId") taskId: String): String

  @Query(
      "select r.key.rootContextId from RootContextIdMapping r " +
          "where r.key.aggregateType " +
          "=  'PARTICIPANT' " +
          "and r.key.aggregateIdentifier = :participantId ")
  fun findProjectIdByParticipantId(@Param("participantId") participantId: String): String

  @Query(
      "select case when (count(r) > 0) then true else false end from RootContextIdMapping r " +
          "where r.key.aggregateType " +
          "= 'PROJECT' " +
          "and r.key.aggregateIdentifier = :projectId ")
  fun existsProjectById(@Param("projectId") projectId: String): Boolean

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
