package com.novatecgmbh.eventsourcing.axon.project.project.web

import com.novatecgmbh.eventsourcing.axon.project.project.api.*
import com.novatecgmbh.eventsourcing.axon.project.project.command.ProjectId
import com.novatecgmbh.eventsourcing.axon.project.project.query.ProjectProjection
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
  fun getAllProjects(): CompletableFuture<List<ProjectProjection>> =
      queryGateway.queryMany(AllProjectsQuery())

  @GetMapping(produces = [APPLICATION_NDJSON_VALUE])
  fun getAllProjectsAndUpdates(): Flux<ProjectProjection> {
    val query =
        queryGateway.subscriptionQuery(
            AllProjectsQuery(),
            ResponseTypes.multipleInstancesOf(ProjectProjection::class.java),
            ResponseTypes.instanceOf(ProjectProjection::class.java))

    return query.initialResult().flatMapMany { Flux.fromIterable(it) }.concatWith(query.updates())
  }

  @GetMapping("/{projectId}")
  fun getProjectById(
      @PathVariable("projectId") projectId: ProjectId
  ): ResponseEntity<ProjectProjection> =
      queryGateway
          .queryOptional<ProjectProjection, ProjectQuery>(ProjectQuery(projectId))
          .join()
          .map { ResponseEntity(it, HttpStatus.OK) }
          .orElse(ResponseEntity(HttpStatus.NOT_FOUND))

  @GetMapping(path = ["/{projectId}"], produces = [APPLICATION_NDJSON_VALUE])
  fun getProjectByIdAndUpdates(
      @PathVariable("projectId") projectId: ProjectId
  ): Flux<ProjectProjection> {
    val query =
        queryGateway.subscriptionQuery(
            ProjectQuery(projectId),
            ResponseTypes.instanceOf(ProjectProjection::class.java),
            ResponseTypes.instanceOf(ProjectProjection::class.java))

    return query.initialResult().concatWith(query.updates())
  }

  @PostMapping
  fun createProject(@RequestBody project: CreateProjectDto): CompletableFuture<String> =
      createProjectWithId(ProjectId(), project)

  @PostMapping("/{projectId}")
  fun createProjectWithId(
      @PathVariable("projectId") projectId: ProjectId,
      @RequestBody project: CreateProjectDto,
  ): CompletableFuture<String> =
      commandGateway.send(
          CreateProjectCommand(
              projectId,
              project.name,
              project.plannedStartDate,
              project.deadline,
          ))

  @PostMapping("/{projectId}/rename")
  fun renameProject(
      @PathVariable("projectId") projectId: ProjectId,
      @RequestBody dto: RenameProjectDto,
  ): CompletableFuture<Unit> =
      commandGateway.send(
          RenameProjectCommand(
              projectId,
              dto.aggregateVersion,
              dto.name,
          ))

  @PostMapping("/{projectId}/reschedule")
  fun renameProject(
      @PathVariable("projectId") projectId: ProjectId,
      @RequestBody dto: RescheduleProjectDto,
  ): CompletableFuture<Unit> =
      commandGateway.send(
          RescheduleProjectCommand(
              projectId,
              dto.aggregateVersion,
              dto.newStartDate,
              dto.newDeadline,
          ))
}
