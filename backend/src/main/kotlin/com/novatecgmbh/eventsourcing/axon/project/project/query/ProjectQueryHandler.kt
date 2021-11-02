package com.novatecgmbh.eventsourcing.axon.project.project.query

import com.novatecgmbh.eventsourcing.axon.application.auditing.AuditUserId
import com.novatecgmbh.eventsourcing.axon.project.authorization.ProjectAclRepository
import com.novatecgmbh.eventsourcing.axon.project.authorization.ProjectAuthorizationService
import com.novatecgmbh.eventsourcing.axon.project.project.api.MyProjectsQuery
import com.novatecgmbh.eventsourcing.axon.project.project.api.ProjectId
import com.novatecgmbh.eventsourcing.axon.project.project.api.ProjectQuery
import com.novatecgmbh.eventsourcing.axon.project.project.api.ProjectQueryResult
import com.novatecgmbh.eventsourcing.axon.user.api.UserId
import java.util.*
import org.axonframework.queryhandling.QueryHandler
import org.springframework.stereotype.Component

@Component
class ProjectQueryHandler(
    private val repository: ProjectProjectionRepository,
    private val authService: ProjectAuthorizationService,
    private val aclRepository: ProjectAclRepository
) {

  @QueryHandler
  fun handle(query: ProjectQuery, @AuditUserId userId: String): Optional<ProjectQueryResult> =
      authService.runWhenAuthorizedForProject(UserId(userId), query.projectId) {
        repository.findById(query.projectId).map { it.toQueryResult() }
      }

  @QueryHandler
  fun handle(query: MyProjectsQuery, @AuditUserId userId: String): Iterable<ProjectQueryResult> =
      aclRepository.findAllAccessibleProjectsByUser(UserId(userId)).map { ProjectId(it) }.let {
        repository.findAllByIdentifierIn(it).map(ProjectProjection::toQueryResult)
      }
}
