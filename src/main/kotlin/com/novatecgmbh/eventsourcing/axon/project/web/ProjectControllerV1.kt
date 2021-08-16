package com.novatecgmbh.eventsourcing.axon.project.web

import com.novatecgmbh.eventsourcing.axon.project.api.AllProjectsQuery
import com.novatecgmbh.eventsourcing.axon.project.api.CreateProjectCommand
import com.novatecgmbh.eventsourcing.axon.project.api.ProjectQuery
import com.novatecgmbh.eventsourcing.axon.project.api.UpdateProjectCommand
import com.novatecgmbh.eventsourcing.axon.project.query.ProjectEntity
import com.novatecgmbh.eventsourcing.axon.project.web.dto.ProjectCreationDto
import java.time.LocalDate
import java.util.*
import java.util.concurrent.CompletableFuture
import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.extensions.kotlin.queryMany
import org.axonframework.extensions.kotlin.queryOptional
import org.axonframework.messaging.responsetypes.ResponseTypes
import org.axonframework.queryhandling.QueryGateway
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/** REST API where creation and update of a resource also return the changed resource */
@RequestMapping("/v1/projects")
@RestController
class ProjectControllerV1(
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
  fun createProject(@RequestBody project: ProjectCreationDto): ResponseEntity<ProjectEntity> =
      createProjectWithId(UUID.randomUUID().toString(), project)

  @PostMapping("/{projectId}")
  fun createProjectWithId(
      @PathVariable("projectId") projectId: String,
      @RequestBody project: ProjectCreationDto,
  ): ResponseEntity<ProjectEntity> =
      queryGateway.subscriptionQuery(
              ProjectQuery(projectId),
              ResponseTypes.instanceOf(ProjectEntity::class.java),
              ResponseTypes.instanceOf(ProjectEntity::class.java),
          )
          .use {
            commandGateway.sendAndWait<Unit>(
                CreateProjectCommand(
                    projectId,
                    project.projectName,
                    project.plannedStartDate,
                    project.deadline,
                )
            )
            val projectEntity = it.updates().blockFirst()
            return ResponseEntity.ok(projectEntity)
          }

  @PutMapping("/{projectId}")
  fun updateProject(
      @PathVariable("projectId") projectId: String,
      @RequestBody project: ProjectUpdateDto,
  ): ResponseEntity<ProjectEntity> =
      queryGateway.subscriptionQuery(
              ProjectQuery(projectId),
              ResponseTypes.instanceOf(ProjectEntity::class.java),
              ResponseTypes.instanceOf(ProjectEntity::class.java),
          )
          .use {
            val expectedAggregateVersion =
                commandGateway.sendAndWait<Long>(
                    UpdateProjectCommand(
                        project.aggregateVersion,
                        projectId,
                        project.projectName,
                        project.plannedStartDate,
                        project.deadline,
                    )
                )
            val projectEntity =
                it.updates()
                    .startWith(it.initialResult().block())
                    .skipUntil { entity -> entity.aggregateVersion == expectedAggregateVersion }
                    .blockFirst()
            return ResponseEntity.ok(projectEntity)
          }
}

data class ProjectUpdateDto(
    val aggregateVersion: Long,
    val projectName: String,
    val plannedStartDate: LocalDate,
    val deadline: LocalDate,
)
