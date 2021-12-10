package com.novatecgmbh.eventsourcing.axon.application.rsocket

import com.novatecgmbh.eventsourcing.axon.application.security.SecurityContextHelper
import com.novatecgmbh.eventsourcing.axon.project.project.api.MyProjectsQuery
import com.novatecgmbh.eventsourcing.axon.project.project.api.ProjectQueryResult
import com.novatecgmbh.eventsourcing.axon.user.api.UserId
import org.axonframework.extensions.reactor.queryhandling.gateway.ReactorQueryGateway
import org.axonframework.messaging.responsetypes.ResponseTypes
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.stereotype.Controller
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Controller
class ProjectController(val queryGateway: ReactorQueryGateway) {

  @MessageMapping("projects")
  fun getMyProjects(): Mono<List<ProjectQueryResult>> {
    val userId = UserId("728ff5ef-2e9e-4573-8786-d1cbf47e1476")
    SecurityContextHelper.setAuthentication(userId.identifier)
    return queryGateway.query(
        MyProjectsQuery(userId), ResponseTypes.multipleInstancesOf(ProjectQueryResult::class.java))
  }

  @MessageMapping("projects")
  fun subscribeMyProjects(): Flux<ProjectQueryResult> {
    val userId = UserId("728ff5ef-2e9e-4573-8786-d1cbf47e1476")
    SecurityContextHelper.setAuthentication(userId.identifier)
    return queryGateway.queryUpdates(MyProjectsQuery(userId), ProjectQueryResult::class.java)
  }
}
