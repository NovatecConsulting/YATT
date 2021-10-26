package com.novatecgmbh.eventsourcing.axon.project.project.web

import com.novatecgmbh.eventsourcing.axon.project.project.api.*
import com.novatecgmbh.eventsourcing.axon.project.project.web.dto.CreateProjectDto
import com.novatecgmbh.eventsourcing.axon.project.project.web.dto.RenameProjectDto
import com.novatecgmbh.eventsourcing.axon.project.project.web.dto.RescheduleProjectDto
import java.util.concurrent.CompletableFuture
import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.extensions.kotlin.queryMany
import org.axonframework.extensions.kotlin.queryOptional
import org.axonframework.messaging.responsetypes.ResponseTypes
import org.axonframework.queryhandling.QueryGateway
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType.APPLICATION_NDJSON_VALUE
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux

/**
 * REST API where creation only returns id of created resource. Single Endpoints for every command,
 * which only returns whether it was successful or not.
 */
@RequestMapping("/v2/projects")
@RestController
class ProjectControllerV2(
    private val commandGateway: CommandGateway,
    private val queryGateway: QueryGateway,
) {
  @GetMapping
  fun getAllProjects(): CompletableFuture<List<ProjectQueryResult>> =
      queryGateway.queryMany(AllProjectsQuery())

  @GetMapping(produces = [APPLICATION_NDJSON_VALUE])
  fun getAllProjectsAndUpdates(): Flux<ProjectQueryResult> {
    val query =
        queryGateway.subscriptionQuery(
            AllProjectsQuery(),
            ResponseTypes.multipleInstancesOf(ProjectQueryResult::class.java),
            ResponseTypes.instanceOf(ProjectQueryResult::class.java))

    return query
        .initialResult()
        .flatMapMany { Flux.fromIterable(it) }
        .concatWith(query.updates())
        .doFinally { query.cancel() }
  }

  @GetMapping("/{projectId}")
  fun getProjectById(
      @PathVariable("projectId") projectId: ProjectId
  ): ResponseEntity<ProjectQueryResult> =
      queryGateway
          .queryOptional<ProjectQueryResult, ProjectQuery>(ProjectQuery(projectId))
          .join()
          .map { ResponseEntity(it, HttpStatus.OK) }
          .orElse(ResponseEntity(HttpStatus.NOT_FOUND))

  @GetMapping(path = ["/{projectId}"], produces = [APPLICATION_NDJSON_VALUE])
  fun getProjectByIdAndUpdates(
      @PathVariable("projectId") projectId: ProjectId
  ): Flux<ProjectQueryResult> {
    val query =
        queryGateway.subscriptionQuery(
            ProjectQuery(projectId),
            ResponseTypes.instanceOf(ProjectQueryResult::class.java),
            ResponseTypes.instanceOf(ProjectQueryResult::class.java))

    return query.initialResult().concatWith(query.updates()).doFinally { query.cancel() }
  }

  @GetMapping("/{projectId}/details")
  fun getProjectDetailsById(
      @PathVariable("projectId") projectId: ProjectId
  ): ResponseEntity<ProjectDetailsQueryResult> =
      queryGateway
          .queryOptional<ProjectDetailsQueryResult, ProjectDetailsQuery>(
              ProjectDetailsQuery(projectId))
          .join()
          .map { ResponseEntity(it, HttpStatus.OK) }
          .orElse(ResponseEntity(HttpStatus.NOT_FOUND))

  @GetMapping(path = ["/{projectId}/details"], produces = [APPLICATION_NDJSON_VALUE])
  fun getProjectDetailsByIdAndUpdates(
      @PathVariable("projectId") projectId: ProjectId
  ): Flux<ProjectDetailsQueryResult> {
    val query =
        queryGateway.subscriptionQuery(
            ProjectDetailsQuery(projectId),
            ResponseTypes.instanceOf(ProjectDetailsQueryResult::class.java),
            ResponseTypes.instanceOf(ProjectDetailsQueryResult::class.java))

    return query.initialResult().concatWith(query.updates()).doFinally { query.cancel() }
  }

  @PostMapping
  fun createProject(@RequestBody body: CreateProjectDto): CompletableFuture<ProjectId> =
      createProjectWithId(ProjectId(), body)

  @PostMapping("/{projectId}")
  fun createProjectWithId(
      @PathVariable("projectId") projectId: ProjectId,
      @RequestBody body: CreateProjectDto,
  ): CompletableFuture<ProjectId> = commandGateway.send(body.toCommand(projectId))

  @PostMapping("/{projectId}/rename")
  fun renameProject(
      @PathVariable("projectId") projectId: ProjectId,
      @RequestBody body: RenameProjectDto,
  ): CompletableFuture<Unit> = commandGateway.send(body.toCommand(projectId))

  @PostMapping("/{projectId}/reschedule")
  fun renameProject(
      @PathVariable("projectId") projectId: ProjectId,
      @RequestBody body: RescheduleProjectDto,
  ): CompletableFuture<Unit> = commandGateway.send(body.toCommand(projectId))
}
