package com.novatecgmbh.eventsourcing.axon.project.project.graphql

import com.novatecgmbh.eventsourcing.axon.application.security.RegisteredUserPrincipal
import com.novatecgmbh.eventsourcing.axon.company.company.api.CompanyId
import com.novatecgmbh.eventsourcing.axon.project.project.api.*
import java.time.LocalDate
import java.util.concurrent.CompletableFuture
import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.extensions.kotlin.queryMany
import org.axonframework.extensions.kotlin.queryOptional
import org.axonframework.messaging.responsetypes.ResponseTypes
import org.axonframework.queryhandling.QueryGateway
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.MutationMapping
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.graphql.data.method.annotation.SubscriptionMapping
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.stereotype.Controller
import reactor.core.publisher.Flux

@Controller
class ProjectController(val commandGateway: CommandGateway, val queryGateway: QueryGateway) {

  @QueryMapping
  fun project(@Argument identifier: ProjectId): CompletableFuture<ProjectQueryResult?> =
      queryGateway.queryOptional<ProjectQueryResult, ProjectQuery>(ProjectQuery(identifier))
          .thenApply { optional -> optional.orElse(null) }

  @QueryMapping
  fun projects(
      @AuthenticationPrincipal user: UsernamePasswordAuthenticationToken
  ): CompletableFuture<List<ProjectQueryResult>> =
      queryGateway.queryMany(
          MyProjectsQuery((user.principal as RegisteredUserPrincipal).identifier))

  @SubscriptionMapping
  fun projectsAndUpdates(
      @AuthenticationPrincipal user: UsernamePasswordAuthenticationToken
  ): Flux<ProjectQueryResult> {
    val query =
        queryGateway.subscriptionQuery(
            MyProjectsQuery((user.principal as RegisteredUserPrincipal).identifier),
            ResponseTypes.multipleInstancesOf(ProjectQueryResult::class.java),
            ResponseTypes.instanceOf(ProjectQueryResult::class.java))

    return query
        .initialResult()
        .flatMapMany { Flux.fromIterable(it) }
        .concatWith(query.updates())
        .doFinally { query.cancel() }
  }

  @MutationMapping
  fun createProject(
      @Argument projectName: String,
      @Argument plannedStartDate: LocalDate,
      @Argument deadline: LocalDate,
      @Argument companyId: CompanyId
  ): CompletableFuture<ProjectId> =
      commandGateway.send(
          CreateProjectCommand(ProjectId(), projectName, plannedStartDate, deadline, companyId))

  @MutationMapping
  fun renameProject(
      @Argument identifier: ProjectId,
      @Argument version: Long,
      @Argument name: String
  ): CompletableFuture<Long> = commandGateway.send(RenameProjectCommand(identifier, version, name))

  @MutationMapping
  fun rescheduleProject(
      @Argument identifier: ProjectId,
      @Argument version: Long,
      @Argument startDate: LocalDate,
      @Argument deadline: LocalDate
  ): CompletableFuture<Long> =
      commandGateway.send(RescheduleProjectCommand(identifier, version, startDate, deadline))
}
