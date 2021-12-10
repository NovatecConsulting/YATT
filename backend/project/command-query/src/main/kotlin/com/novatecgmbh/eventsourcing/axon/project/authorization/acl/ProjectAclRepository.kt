package com.novatecgmbh.eventsourcing.axon.project.authorization.acl

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
          "= com.novatecgmbh.eventsourcing.axon.project.authorization.acl.AuthorizableAggregateTypesEnum.PROJECT " +
          "and p.key.permission " +
          "= com.novatecgmbh.eventsourcing.axon.project.authorization.acl.PermissionEnum.ACCESS_PROJECT " +
          "and p.key.userId = :userId")
  fun findAllAccessibleProjectsByUser(@Param("userId") userId: UserId): List<String>

  @Query(
      "select p.key.userId from ProjectAcl p " +
          "where p.key.aggregateType " +
          "= com.novatecgmbh.eventsourcing.axon.project.authorization.acl.AuthorizableAggregateTypesEnum.PROJECT " +
          "and p.key.aggregateIdentifier = :projectId " +
          "and p.key.permission " +
          "= com.novatecgmbh.eventsourcing.axon.project.authorization.acl.PermissionEnum.ACCESS_PROJECT")
  fun findAllUserWithAccessToProject(@Param("projectId") projectId: String): List<UserId>

  @Query(
      "select case when (count(p) > 0) then true else false end from ProjectAcl p " +
          "where p.key.aggregateType " +
          "= com.novatecgmbh.eventsourcing.axon.project.authorization.acl.AuthorizableAggregateTypesEnum.PROJECT " +
          "and p.key.aggregateIdentifier = :projectId " +
          "and p.key.permission " +
          "= com.novatecgmbh.eventsourcing.axon.project.authorization.acl.PermissionEnum.ACCESS_PROJECT " +
          "and p.key.userId = :userId")
  fun hasAccessToProject(
      @Param("userId") userId: UserId,
      @Param("projectId") projectId: String
  ): Boolean

  @Query(
      "select p.key.aggregateIdentifier from ProjectAcl p " +
          "where p.key.aggregateType " +
          "= com.novatecgmbh.eventsourcing.axon.project.authorization.acl.AuthorizableAggregateTypesEnum.PROJECT " +
          "and p.key.aggregateIdentifier in :projectIds " +
          "and p.key.permission " +
          "= com.novatecgmbh.eventsourcing.axon.project.authorization.acl.PermissionEnum.ACCESS_PROJECT " +
          "and p.key.userId = :userId")
  fun filterProjectsWithAccess(
      @Param("userId") userId: UserId,
      @Param("projectIds") projectIds: Set<String>
  ): List<String>

  @Query(
      "select case when (count(p) > 0) then true else false end from ProjectAcl p " +
          "where p.key.aggregateType " +
          "= com.novatecgmbh.eventsourcing.axon.project.authorization.acl.AuthorizableAggregateTypesEnum.COMPANY " +
          "and p.key.aggregateIdentifier = :companyId " +
          "and p.key.permission " +
          "= com.novatecgmbh.eventsourcing.axon.project.authorization.acl.PermissionEnum.CREATE_PROJECT " +
          "and p.key.userId = :userId")
  fun hasPermissionToCreateProjectForCompany(
      @Param("userId") userId: UserId,
      @Param("companyId") companyId: String
  ): Boolean
}
