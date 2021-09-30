package com.novatecgmbh.eventsourcing.axon.project.project.web

import com.novatecgmbh.eventsourcing.axon.project.project.api.*
import com.novatecgmbh.eventsourcing.axon.project.project.web.dto.CreateProjectDto
import java.time.Duration
import java.time.LocalDate
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
  fun createProject(
      @RequestBody project: CreateProjectDto
  ): Mono<ResponseEntity<ProjectQueryResult>> = createProjectWithId(ProjectId(), project)

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
                      commandGateway.send<Unit>(
                          CreateProjectCommand(
                              projectId,
                              project.name,
                              project.plannedStartDate,
                              project.deadline,
                          ))
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
      @RequestBody project: ProjectUpdateDto,
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
                      commandGateway.send<Long>(
                          UpdateProjectCommand(
                              projectId,
                              project.version,
                              project.name,
                              project.plannedStartDate,
                              project.deadline,
                          ))
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

data class ProjectUpdateDto(
    val version: Long,
    val name: String,
    val plannedStartDate: LocalDate,
    val deadline: LocalDate,
)
