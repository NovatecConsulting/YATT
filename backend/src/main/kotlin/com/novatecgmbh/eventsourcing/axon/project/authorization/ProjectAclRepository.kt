package com.novatecgmbh.eventsourcing.axon.project.authorization

import com.novatecgmbh.eventsourcing.axon.user.api.UserId
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface ProjectAclRepository : JpaRepository<ProjectAcl, ProjectAclKey> {

  @Query(
      "select p.key.aggregateIdentifier from ProjectAcl p " +
          "where p.key.aggregateType " +
          "= com.novatecgmbh.eventsourcing.axon.project.authorization.AuthorizableAggregateTypesEnum.PROJECT " +
          "and p.key.permission " +
          "= com.novatecgmbh.eventsourcing.axon.project.authorization.PermissionEnum.ACCESS_PROJECT " +
          "and p.key.userId = :userId")
  fun findAllAccessibleProjectsByUser(@Param("userId") userId: UserId): List<String>

  @Query(
      "select p.key.userId from ProjectAcl p " +
          "where p.key.aggregateType " +
          "= com.novatecgmbh.eventsourcing.axon.project.authorization.AuthorizableAggregateTypesEnum.PROJECT " +
          "and p.key.aggregateIdentifier = :projectId " +
          "and p.key.permission " +
          "= com.novatecgmbh.eventsourcing.axon.project.authorization.PermissionEnum.ACCESS_PROJECT")
  fun findAllUserWithAccessToProject(@Param("projectId") projectId: String): List<UserId>

  @Query(
      "select case when (count(p) > 0) then true else false end from ProjectAcl p " +
          "where p.key.aggregateType " +
          "= com.novatecgmbh.eventsourcing.axon.project.authorization.AuthorizableAggregateTypesEnum.PROJECT " +
          "and p.key.aggregateIdentifier = :projectId " +
          "and p.key.permission " +
          "= com.novatecgmbh.eventsourcing.axon.project.authorization.PermissionEnum.ACCESS_PROJECT " +
          "and p.key.userId = :userId")
  fun hasAccessToProject(
      @Param("userId") userId: UserId,
      @Param("projectId") projectId: String
  ): Boolean
}
