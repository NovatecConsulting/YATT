package com.novatecgmbh.eventsourcing.axon.web

import com.novatecgmbh.eventsourcing.axon.coreapi.AllProjectsQuery
import com.novatecgmbh.eventsourcing.axon.coreapi.CreateProjectCommand
import com.novatecgmbh.eventsourcing.axon.coreapi.ProjectQuery
import com.novatecgmbh.eventsourcing.axon.query.ProjectEntity
import com.novatecgmbh.eventsourcing.axon.web.dto.ProjectCreationDto
import java.util.*
import java.util.concurrent.CompletableFuture
import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.messaging.responsetypes.ResponseTypes
import org.axonframework.queryhandling.QueryGateway
import org.springframework.web.bind.annotation.*

@RequestMapping("/projects")
@RestController
class ProjectController(
    private val commandGateway: CommandGateway,
    private val queryGateway: QueryGateway,
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
  fun createProject(@RequestBody project: ProjectCreationDto): CompletableFuture<String> =
      commandGateway.send(
          CreateProjectCommand(
              UUID.randomUUID().toString(),
              project.projectName,
              project.plannedStartDate,
              project.deadline,
          ))

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
          ))

  //    @PutMapping("/{projectId}")
  //    fun updateProject(
  //        @PathVariable("projectId") projectId: String,
  //        @RequestBody project: ProjectCreationDto,
  //    ) {
  //
  //    }
}
