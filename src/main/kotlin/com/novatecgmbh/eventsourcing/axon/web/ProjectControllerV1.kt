package com.novatecgmbh.eventsourcing.axon.web

import com.novatecgmbh.eventsourcing.axon.command.Project
import com.novatecgmbh.eventsourcing.axon.coreapi.*
import com.novatecgmbh.eventsourcing.axon.query.ProjectEntity
import com.novatecgmbh.eventsourcing.axon.web.dto.ProjectCreateUpdateDto
import java.util.*
import java.util.concurrent.CompletableFuture
import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.messaging.responsetypes.ResponseTypes
import org.axonframework.modelling.command.Repository
import org.axonframework.queryhandling.QueryGateway
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * REST API where creation and update of a resource also return the changed resource
 */
@RequestMapping("/v1/projects")
@RestController
class ProjectControllerV1(
    private val commandGateway: CommandGateway,
    private val queryGateway: QueryGateway,
    private val projectRepository: Repository<Project>,
) {
  @GetMapping
  fun getAllProjects(): CompletableFuture<List<ProjectEntity>> =
      queryGateway.query(
          AllProjectsQuery(), ResponseTypes.multipleInstancesOf(ProjectEntity::class.java))

  @GetMapping("/{projectId}")
  fun getProjectById(
      @PathVariable("projectId") projectId: String
  ): CompletableFuture<Optional<ProjectEntity>> =
      queryGateway.query(
          ProjectQuery(projectId), ResponseTypes.optionalInstanceOf(ProjectEntity::class.java))

  @PostMapping
  fun createProject(@RequestBody project: ProjectCreateUpdateDto): ResponseEntity<ProjectEntity> =
      createProjectWithId(UUID.randomUUID().toString(), project)

  @PostMapping("/{projectId}")
  fun createProjectWithId(
      @PathVariable("projectId") projectId: String,
      @RequestBody project: ProjectCreateUpdateDto,
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
                ))
            val projectEntity = it.updates().blockFirst()
            return ResponseEntity.ok(projectEntity)
          }

  /* Very bad, because aggregates should not be queried. Also not working because fields of aggregate are private.
     Could also cause problems because of other concurrent requests on same aggregate.
    @PutMapping("/{projectId}")
    fun updateProjectV1(
        @PathVariable("projectId") projectId: String,
        @RequestBody project: ProjectCreateUpdateDto,
    ): ResponseEntity<ProjectEntity> =
        projectRepository.load(projectId).invoke { projectAggregate ->
          queryGateway.subscriptionQuery(
                  ProjectQuery(projectId),
                  ResponseTypes.instanceOf(ProjectEntity::class.java),
                  ResponseTypes.instanceOf(ProjectEntity::class.java),
              )
              .use {
                var commandCount = 0
                if (projectAggregate.projectName != project.projectName) {
                  commandGateway.sendAndWait<Unit>(
                      RenameProjectCommand(
                          projectId,
                          project.projectName,
                      ))
                  commandCount++
                }
                if (projectAggregate.plannedStartDate != project.plannedStartDate ||
                    projectAggregate.deadline != project.deadline) {
                  commandGateway.sendAndWait<Unit>(
                      RescheduleProjectCommand(
                          projectId,
                          project.plannedStartDate,
                          project.deadline,
                      ))
                  commandCount++
                }

                if (0 == commandCount) {
                  return@invoke ResponseEntity.ok(it.initialResult().block())
                }

                val projectEntity = it.updates().skip(commandCount - 1L).blockFirst()
                return@invoke ResponseEntity.ok(projectEntity)
              }
        }
  */

  /**
   * Works, but always sends commands to change all attributes, which will trigger events, even if
   * nothing changed. Could also cause problems because of other concurrent requests on same
   * aggregate.
   */
  @PutMapping("/{projectId}")
  fun updateProjectV2(
      @PathVariable("projectId") projectId: String,
      @RequestBody project: ProjectCreateUpdateDto,
  ): ResponseEntity<ProjectEntity> =
      queryGateway.subscriptionQuery(
              ProjectQuery(projectId),
              ResponseTypes.instanceOf(ProjectEntity::class.java),
              ResponseTypes.instanceOf(ProjectEntity::class.java),
          )
          .use {
            commandGateway.sendAndWait<Unit>(
                RenameProjectCommand(
                    projectId,
                    project.projectName,
                ))
            commandGateway.sendAndWait<Unit>(
                RescheduleProjectCommand(
                    projectId,
                    project.plannedStartDate,
                    project.deadline,
                ))

            val commandCount = 2
            val projectEntity = it.updates().skip(commandCount - 1L).blockFirst()
            return ResponseEntity.ok(projectEntity)
          }
}
