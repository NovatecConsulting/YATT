package com.novatecgmbh.eventsourcing.axon.project.project.command

import com.novatecgmbh.eventsourcing.axon.common.command.AlreadyExistsException
import com.novatecgmbh.eventsourcing.axon.company.company.api.CompanyId
import com.novatecgmbh.eventsourcing.axon.project.project.api.*
import com.novatecgmbh.eventsourcing.axon.project.project.api.ProjectStatus.ON_TIME
import java.time.LocalDate
import org.axonframework.modelling.command.ConflictingAggregateVersionException
import org.axonframework.test.aggregate.AggregateTestFixture
import org.axonframework.test.aggregate.FixtureConfiguration
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ProjectTest {
  private lateinit var fixture: FixtureConfiguration<Project>

  @BeforeEach
  fun setUp() {
    fixture = AggregateTestFixture(Project::class.java)
  }

  companion object {
    private val companyId = CompanyId()
    val createProjectCommand =
        CreateProjectCommand(
            aggregateIdentifier = ProjectId("1"),
            projectName = "test",
            plannedStartDate = LocalDate.of(2021, 1, 1),
            deadline = LocalDate.of(2022, 1, 1),
            companyId = companyId)
    val projectCreatedEvent =
        ProjectCreatedEvent(
            aggregateIdentifier = ProjectId("1"),
            projectName = "test",
            plannedStartDate = LocalDate.of(2021, 1, 1),
            deadline = LocalDate.of(2022, 1, 1),
            companyId = companyId,
            status = ON_TIME
        )
    val renameProjectCommand =
        RenameProjectCommand(
            aggregateIdentifier = ProjectId("1"),
            aggregateVersion = 0,
            newName = "new name",
        )
    val projectRenamedEvent =
        ProjectRenamedEvent(
            aggregateIdentifier = ProjectId("1"),
            newName = "new name",
        )
    val rescheduleProjectCommand =
        RescheduleProjectCommand(
            aggregateIdentifier = ProjectId("1"),
            aggregateVersion = 0,
            newStartDate = LocalDate.of(2021, 1, 1),
            newDeadline = LocalDate.of(2022, 1, 1),
        )
    val projectRescheduledEvent =
        ProjectRescheduledEvent(
            aggregateIdentifier = ProjectId("1"),
            newStartDate = LocalDate.of(2021, 1, 1),
            newDeadline = LocalDate.of(2022, 1, 1),
        )
  }

  @Nested
  inner class CreateProjectCommandTests {
    @Test
    fun `should create project if project does not exist`() {
      fixture
          .givenNoPriorActivity()
          .`when`(createProjectCommand)
          .expectResultMessagePayload(ProjectId("1"))
          .expectSuccessfulHandlerExecution()
          .expectEvents(projectCreatedEvent)
    }

    @Test
    fun `should throw exception if project already exists`() {
      fixture
          .given(projectCreatedEvent)
          .`when`(createProjectCommand)
          .expectException(AlreadyExistsException::class.java)
    }

    @Test
    fun `should throw exception if start date is after deadline`() {
      fixture
          .givenNoPriorActivity()
          .`when`(
              createProjectCommand.copy(
                  plannedStartDate = LocalDate.of(2022, 1, 1),
                  deadline = LocalDate.of(2021, 1, 1),
              ))
          .expectException(IllegalArgumentException::class.java)
    }
  }

  @Nested
  inner class RenameProjectCommandTests {
    @Test
    fun `should rename project if new name is different to current`() {
      fixture
          .given(projectCreatedEvent)
          .`when`(renameProjectCommand)
          .expectResultMessagePayload(1L)
          .expectSuccessfulHandlerExecution()
          .expectEvents(projectRenamedEvent)
    }

    @Test
    fun `should not rename project if new name is same as current`() {
      fixture
          .given(projectCreatedEvent)
          .`when`(renameProjectCommand.copy(newName = projectCreatedEvent.projectName))
          .expectResultMessagePayload(0L)
          .expectSuccessfulHandlerExecution()
          .expectNoEvents()
    }

    @Test
    fun `should throw exception if renaming is based on wrong version`() {
      fixture
          .given(projectCreatedEvent, projectRenamedEvent)
          .`when`(renameProjectCommand.copy(newName = projectRenamedEvent.newName + "new"))
          .expectException(ConflictingAggregateVersionException::class.java)
    }

    @Test
    fun `should not throw or rename if renaming is based on wrong version but new name is same as current`() {
      fixture
          .given(
              projectCreatedEvent,
              projectRenamedEvent.copy(newName = "test2"),
              projectRenamedEvent.copy(newName = "test3"),
          )
          .`when`(renameProjectCommand.copy(newName = "test3"))
          .expectResultMessagePayload(2L)
          .expectSuccessfulHandlerExecution()
          .expectNoEvents()
    }
  }

  @Nested
  inner class RescheduleProjectCommandTests {
    @Test
    fun `should reschedule project if start date is different from current`() {
      val newStartDate = projectCreatedEvent.plannedStartDate.minusDays(1)

      fixture
          .given(projectCreatedEvent)
          .`when`(rescheduleProjectCommand.copy(newStartDate = newStartDate))
          .expectResultMessagePayload(1L)
          .expectSuccessfulHandlerExecution()
          .expectEvents(projectRescheduledEvent.copy(newStartDate = newStartDate))
    }

    @Test
    fun `should reschedule project if deadline is different from current`() {
      val newDeadline = projectCreatedEvent.deadline.plusDays(1)

      fixture
          .given(projectCreatedEvent)
          .`when`(rescheduleProjectCommand.copy(newDeadline = newDeadline))
          .expectResultMessagePayload(1L)
          .expectSuccessfulHandlerExecution()
          .expectEvents(projectRescheduledEvent.copy(newDeadline = newDeadline))
    }

    @Test
    fun `should not reschedule project if new start date and deadline is same as current`() {
      fixture
          .given(projectCreatedEvent)
          .`when`(rescheduleProjectCommand)
          .expectResultMessagePayload(0L)
          .expectSuccessfulHandlerExecution()
          .expectNoEvents()
    }

    @Test
    fun `should throw exception if rescheduling is based on wrong version`() {
      val newDeadline1 = projectCreatedEvent.deadline.plusDays(1)
      val newDeadline2 = projectCreatedEvent.deadline.plusDays(2)

      fixture
          .given(projectCreatedEvent, projectRescheduledEvent.copy(newDeadline = newDeadline1))
          .`when`(rescheduleProjectCommand.copy(newDeadline = newDeadline2))
          .expectException(ConflictingAggregateVersionException::class.java)
    }

    @Test
    fun `should not throw or reschedule if renaming is based on wrong version but new dates are same as current`() {
      val newDeadline1 = projectCreatedEvent.deadline.plusDays(1)
      val newDeadline2 = projectCreatedEvent.deadline.plusDays(2)

      fixture
          .given(
              projectCreatedEvent,
              projectRescheduledEvent.copy(newDeadline = newDeadline1),
              projectRescheduledEvent.copy(newDeadline = newDeadline2),
          )
          .`when`(rescheduleProjectCommand.copy(newDeadline = newDeadline2))
          .expectResultMessagePayload(2L)
          .expectSuccessfulHandlerExecution()
          .expectNoEvents()
    }
  }
}
