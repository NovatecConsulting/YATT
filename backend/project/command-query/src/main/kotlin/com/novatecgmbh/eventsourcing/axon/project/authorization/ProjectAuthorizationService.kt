package com.novatecgmbh.eventsourcing.axon.project.authorization

import com.novatecgmbh.eventsourcing.axon.project.authorization.acl.ProjectAclRepository
import com.novatecgmbh.eventsourcing.axon.project.project.api.ProjectId
import com.novatecgmbh.eventsourcing.axon.user.api.UserId
import org.springframework.stereotype.Service

@Service
class ProjectAuthorizationService(private val aclRepository: ProjectAclRepository) {

  fun <T> runWhenAuthorizedForProject(userId: UserId, projectId: ProjectId, callable: () -> T): T =
      if (aclRepository.hasAccessToProject(userId, projectId.toString())) {
        callable.invoke()
      } else {
        throw IllegalAccessException("User has no access to this project")
      }

  fun <T> runWhenAuthorizedForAllProjects(
      userId: UserId,
      projectIds: Set<ProjectId>,
      callable: () -> T
  ): T =
      if (aclRepository.filterProjectsWithAccess(
              userId, projectIds.map(ProjectId::toString).toSet())
          .size == projectIds.size) {
        callable.invoke()
      } else {
        throw IllegalAccessException("User does not have access to all requested projects")
      }
}
