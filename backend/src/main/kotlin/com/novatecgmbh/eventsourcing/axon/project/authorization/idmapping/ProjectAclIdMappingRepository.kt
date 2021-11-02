package com.novatecgmbh.eventsourcing.axon.project.authorization.idmapping

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface ProjectAclIdMappingRepository :
    JpaRepository<ProjectAclIdMapping, ProjectAclIdMappingKey> {

  @Query(
      "select p.key.projectId from ProjectAclIdMapping p " +
          "where p.key.aggregateType " +
          "=  com.novatecgmbh.eventsourcing.axon.project.ProjectContextAggregateTypesEnum.TASK " +
          "and p.key.aggregateIdentifier = :taskId ")
  fun findProjectOfTask(@Param("taskId") taskId: String): String

  @Query(
      "select p.key.projectId from ProjectAclIdMapping p " +
          "where p.key.aggregateType " +
          "=  com.novatecgmbh.eventsourcing.axon.project.ProjectContextAggregateTypesEnum.PARTICIPANT " +
          "and p.key.aggregateIdentifier = :participantId ")
  fun findProjectOfParticipant(@Param("participantId") participantId: String): String
}
