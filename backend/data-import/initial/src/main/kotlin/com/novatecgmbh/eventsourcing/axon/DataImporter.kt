package com.novatecgmbh.eventsourcing.axon

import com.novatecgmbh.eventsourcing.axon.application.security.SecurityContextHelper
import com.novatecgmbh.eventsourcing.axon.company.company.api.CompanyId
import com.novatecgmbh.eventsourcing.axon.company.company.api.CreateCompanyCommand
import com.novatecgmbh.eventsourcing.axon.company.employee.api.CreateEmployeeCommand
import com.novatecgmbh.eventsourcing.axon.company.employee.api.EmployeeId
import com.novatecgmbh.eventsourcing.axon.company.employee.api.GrantProjectManagerPermissionToEmployee
import com.novatecgmbh.eventsourcing.axon.project.project.api.CreateProjectCommand
import com.novatecgmbh.eventsourcing.axon.project.project.api.ProjectId
import com.novatecgmbh.eventsourcing.axon.project.task.api.CreateTaskCommand
import com.novatecgmbh.eventsourcing.axon.project.task.api.TaskId
import com.novatecgmbh.eventsourcing.axon.user.api.RegisterUserCommand
import com.novatecgmbh.eventsourcing.axon.user.api.UserId
import java.time.LocalDate
import java.util.concurrent.CompletableFuture
import kotlin.streams.toList
import org.axonframework.commandhandling.gateway.CommandGateway
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component

const val numProjects = 50
const val numTasksPerProject = 1000

@Component
class DataImporter(private val commandGateway: CommandGateway) : CommandLineRunner {
  private val userIds = listOf(UserId(), UserId(), UserId())
  private val companyIds = listOf(CompanyId(), CompanyId(), CompanyId())
  private val createProjectCommands = mutableMapOf<UserId, MutableList<CreateProjectCommand>>()

  override fun run(vararg args: String?) {
    registerUsers()
    Thread.sleep(2000)

    createCompaniesAndEmployees()
    Thread.sleep(2000)

    createProjects().join()
    Thread.sleep(2000)

    for (mapEntry in createProjectCommands) {
      SecurityContextHelper.setAuthentication(mapEntry.key.identifier)
      for (createProjectCommand in mapEntry.value) {
        try {
          createTasks(createProjectCommand).join()
        } catch (e: Exception) {
          println(e.message)
        }
      }
    }
  }

  private fun registerUsers() {
    println("Creating users...")
    SecurityContextHelper.setAuthentication("system")
    commandGateway
        .send<UserId>(
            RegisterUserCommand(
                userIds[0],
                "af0d09bf-154b-47c9-af4d-2c585c70083d",
                "Max",
                "Mustermann",
                "max.mustermann@gmail.com",
                "+4915112345678"))
        .join()
    commandGateway
        .send<UserId>(
            RegisterUserCommand(
                userIds[1],
                "d13c404d-a803-452f-9d6e-55f7239ca8e0",
                "Claire",
                "Grube",
                "claire.grube@gmail.com",
                "+4917312345678"))
        .join()
    commandGateway
        .send<UserId>(
            RegisterUserCommand(
                userIds[2],
                "ace9d139-0c88-4380-ae32-7e8cf9bda02c",
                "Klaus",
                "Kuester",
                "klaus.kuester@gmail.com",
                "+4917612345678"))
        .join()
  }

  private fun createCompaniesAndEmployees() {
    println("Creating companies...")
    SecurityContextHelper.setAuthentication(userIds[0].identifier)
    commandGateway.send<Unit>(CreateCompanyCommand(companyIds[0], "Novatec Consulting GmbH")).join()
    commandGateway.send<Unit>(CreateCompanyCommand(companyIds[1], "Some Company GmbH")).join()
    commandGateway.send<Unit>(CreateCompanyCommand(companyIds[2], "Another Company AG")).join()

    Thread.sleep(2000)

    println("Creating employees...")
    val employeeIdOfUser0 = EmployeeId()
    val employeeIdOfUser1 = EmployeeId()
    val employeeIdOfUser2 = EmployeeId()
    commandGateway
        .send<Unit>(CreateEmployeeCommand(employeeIdOfUser0, companyIds[0], userIds[0]))
        .join()
    commandGateway.send<Unit>(CreateEmployeeCommand(EmployeeId(), companyIds[0], userIds[1])).join()
    commandGateway
        .send<Unit>(CreateEmployeeCommand(employeeIdOfUser1, companyIds[1], userIds[1]))
        .join()
    commandGateway.send<Unit>(CreateEmployeeCommand(EmployeeId(), companyIds[1], userIds[2])).join()
    commandGateway
        .send<Unit>(CreateEmployeeCommand(employeeIdOfUser2, companyIds[2], userIds[2]))
        .join()
    commandGateway.send<Unit>(CreateEmployeeCommand(EmployeeId(), companyIds[2], userIds[0])).join()

    println("Granting project manager permissions...")
    commandGateway.send<Unit>(GrantProjectManagerPermissionToEmployee(employeeIdOfUser0)).join()
    commandGateway.send<Unit>(GrantProjectManagerPermissionToEmployee(employeeIdOfUser1)).join()
    commandGateway.send<Unit>(GrantProjectManagerPermissionToEmployee(employeeIdOfUser2)).join()
  }

  private fun createProjects(): CompletableFuture<Void> {
    println("Creating projects...")
    val futures = mutableListOf<CompletableFuture<Unit>>()
    for (i in 1..numProjects) {
      val projectId = ProjectId()
      val projectName = "Project $i"
      val projectStartDate =
          LocalDate.of(2019, 1, 1).datesUntil(LocalDate.of(2024, 12, 31)).toList().random()

      val projectDeadline = projectStartDate.plusYears(1)
      val index = userIds.indices.random()
      val userId = userIds[index]

      SecurityContextHelper.setAuthentication(userId.identifier)
      val createProjectCommand =
          CreateProjectCommand(
              projectId, projectName, projectStartDate, projectDeadline, companyIds[index])
      futures.add(commandGateway.send(createProjectCommand))
      createProjectCommands.getOrPut(userId) { mutableListOf() }.add(createProjectCommand)
    }
    return CompletableFuture.allOf(*futures.toTypedArray())
  }

  private fun createTasks(command: CreateProjectCommand): CompletableFuture<Void> {
    println("Creating tasks for project ${command.projectName}...")
    val projectId = command.aggregateIdentifier
    val projectStartDate = command.plannedStartDate
    val projectDeadline = command.deadline

    val futures = mutableListOf<CompletableFuture<Unit>>()
    for (i in 1..numTasksPerProject) {
      val taskStartDate = projectStartDate.datesUntil(projectDeadline).toList().random()
      val taskEndDate = taskStartDate.plusDays((3..30L).random())
      futures.add(
          commandGateway.send(
              CreateTaskCommand(TaskId(), projectId, "Task $i", null, taskStartDate, taskEndDate)))
    }
    return CompletableFuture.allOf(*futures.toTypedArray())
  }
}

fun <T> List<T>.random(): T = shuffled().first()
