package com.novatecgmbh.eventsourcing.axon.project.project.graphql

import com.novatecgmbh.eventsourcing.axon.company.company.api.CompanyId
import com.novatecgmbh.eventsourcing.axon.project.project.api.CreateProjectCommand
import com.novatecgmbh.eventsourcing.axon.project.project.api.ProjectId
import com.novatecgmbh.eventsourcing.axon.project.project.api.RenameProjectCommand
import com.novatecgmbh.eventsourcing.axon.project.project.api.RescheduleProjectCommand
import java.time.LocalDate
import java.util.concurrent.CompletableFuture
import org.axonframework.commandhandling.gateway.CommandGateway
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.MutationMapping
import org.springframework.stereotype.Controller

@Controller
class ProjectMutationsController(val commandGateway: CommandGateway) {

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
