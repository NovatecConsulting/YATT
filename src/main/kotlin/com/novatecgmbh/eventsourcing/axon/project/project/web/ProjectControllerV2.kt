package com.novatecgmbh.eventsourcing.axon.project.project.web

import com.novatecgmbh.eventsourcing.axon.project.project.query.ProjectEntity
import com.novatecgmbh.eventsourcing.axon.project.project.web.dto.ProjectCreationDto
import com.novatecgmbh.eventsourcing.axon.project.project.api.*
import java.time.LocalDate
import java.util.*
import java.util.concurrent.CompletableFuture
import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.extensions.kotlin.queryMany
import org.axonframework.extensions.kotlin.queryOptional
import org.axonframework.queryhandling.QueryGateway
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

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
  fun getAllProjects(): CompletableFuture<List<ProjectEntity>> =
      queryGateway.queryMany(AllProjectsQuery())

  @GetMapping("/{projectId}")
  fun getProjectById(@PathVariable("projectId") projectId: String): ResponseEntity<ProjectEntity> =
      queryGateway
          .queryOptional<ProjectEntity, ProjectQuery>(ProjectQuery(projectId))
          .join()
          .map { ResponseEntity(it, HttpStatus.OK) }
          .orElse(ResponseEntity(HttpStatus.NOT_FOUND))

  @PostMapping
  fun createProject(@RequestBody project: ProjectCreationDto): CompletableFuture<String> =
      createProjectWithId(UUID.randomUUID().toString(), project)

  @PostMapping("/{projectId}")
  fun createProjectWithId(
      @PathVariable("projectId") projectId: String,
      @RequestBody project: ProjectCreationDto,
  ): CompletableFuture<String> =
      commandGateway.send(
          CreateProjectCommand(
              projectId,
              project.projectName,
              project.plannedStartDate,
              project.deadline,
          )
      )

  @PostMapping("/{projectId}/rename")
  fun renameProject(
      @PathVariable("projectId") projectId: String,
      @RequestBody dto: ProjectNameDto,
  ): CompletableFuture<Unit> =
      commandGateway.send(
          RenameProjectCommand(
              dto.aggregateVersion,
              projectId,
              dto.name,
          )
      )

  @PostMapping("/{projectId}/reschedule")
  fun renameProject(
      @PathVariable("projectId") projectId: String,
      @RequestBody dto: ProjectDatesDto,
  ): CompletableFuture<Unit> =
      commandGateway.send(
          RescheduleProjectCommand(
              dto.aggregateVersion,
              projectId,
              dto.newStartDate,
              dto.newDeadline,
          )
      )
}

data class ProjectNameDto(val aggregateVersion: Long, val name: String)

data class ProjectDatesDto(
    val aggregateVersion: Long,
    val newStartDate: LocalDate,
    val newDeadline: LocalDate
)
