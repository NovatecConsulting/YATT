package com.novatecgmbh.eventsourcing.axon.project.project.web

import com.novatecgmbh.eventsourcing.axon.project.project.api.*
import com.novatecgmbh.eventsourcing.axon.project.project.web.dto.CreateProjectDto
import com.novatecgmbh.eventsourcing.axon.project.project.web.dto.UpdateProjectDto
import java.time.Duration
import java.util.concurrent.CompletableFuture
import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.extensions.kotlin.queryMany
import org.axonframework.extensions.kotlin.queryOptional
import org.axonframework.messaging.responsetypes.ResponseTypes
import org.axonframework.queryhandling.QueryGateway
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono

/** REST API where creation and update of a resource also return the changed resource */
@RequestMapping("/v1/projects")
@RestController
class ProjectControllerV1(
    private val commandGateway: CommandGateway,
    private val queryGateway: QueryGateway,
) {
  @GetMapping
  fun getAllProjects(): CompletableFuture<List<ProjectQueryResult>> =
      queryGateway.queryMany(AllProjectsQuery())

  @GetMapping("/{projectId}")
  fun getProjectById(
      @PathVariable("projectId") projectId: ProjectId
  ): ResponseEntity<ProjectQueryResult> =
      queryGateway
          .queryOptional<ProjectQueryResult, ProjectQuery>(ProjectQuery(projectId))
          .join()
          .map { ResponseEntity(it, HttpStatus.OK) }
          .orElse(ResponseEntity(HttpStatus.NOT_FOUND))

  @PostMapping
  fun createProject(@RequestBody body: CreateProjectDto): Mono<ResponseEntity<ProjectQueryResult>> =
      createProjectWithId(ProjectId(), body)

  @PostMapping("/{projectId}")
  fun createProjectWithId(
      @PathVariable("projectId") projectId: ProjectId,
      @RequestBody project: CreateProjectDto,
  ): Mono<ResponseEntity<ProjectQueryResult>> =
      queryGateway.subscriptionQuery(
              ProjectQuery(projectId),
              ResponseTypes.instanceOf(ProjectQueryResult::class.java),
              ResponseTypes.instanceOf(ProjectQueryResult::class.java),
          )
          .let { queryResult ->
            Mono.`when`(queryResult.initialResult())
                .then(
                    Mono.fromCompletionStage {
                      commandGateway.send<Unit>(project.toCommand(projectId))
                    })
                .thenMany(queryResult.updates())
                .next()
                .map { entity -> ResponseEntity.ok(entity) }
                .timeout(Duration.ofSeconds(5))
                .doFinally { queryResult.cancel() }
          }

  @PutMapping("/{projectId}")
  fun updateProject(
      @PathVariable("projectId") projectId: ProjectId,
      @RequestBody body: UpdateProjectDto,
  ): Mono<ResponseEntity<ProjectQueryResult>> =
      queryGateway.subscriptionQuery(
              ProjectQuery(projectId),
              ResponseTypes.instanceOf(ProjectQueryResult::class.java),
              ResponseTypes.instanceOf(ProjectQueryResult::class.java),
          )
          .let { queryResult ->
            Mono.`when`(queryResult.initialResult())
                .then(
                    Mono.fromCompletionStage {
                      commandGateway.send<Long>(body.toCommand(projectId))
                    })
                .flatMap { expectedAggregateVersion ->
                  queryResult
                      .updates()
                      .startWith(queryResult.initialResult())
                      .skipUntil { entity -> entity.version == expectedAggregateVersion }
                      .next()
                }
                .map { entity -> ResponseEntity.ok(entity) }
                .timeout(Duration.ofSeconds(5))
                .doFinally { queryResult.cancel() }
          }
}
