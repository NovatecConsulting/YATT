package com.novatecgmbh.eventsourcing.axon.project.project.command

import com.novatecgmbh.eventsourcing.axon.common.command.AlreadyExistsException
import com.novatecgmbh.eventsourcing.axon.project.project.api.*
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

  @Nested
  inner class CreateProjectCommandTests {
    @Test
    fun `should create project if project does not exist`() {
      val createProjectCommand =
          CreateProjectCommand(
              aggregateIdentifier = ProjectId("1"),
              projectName = "test",
              plannedStartDate = LocalDate.of(2021, 1, 1),
              deadline = LocalDate.of(2022, 1, 1),
          )
      val projectCreatedEvent =
          ProjectCreatedEvent(
              aggregateIdentifier = ProjectId("1"),
              projectName = "test",
              plannedStartDate = LocalDate.of(2021, 1, 1),
              deadline = LocalDate.of(2022, 1, 1),
          )

      fixture
          .givenNoPriorActivity()
          .`when`(createProjectCommand)
          .expectResultMessagePayload(ProjectId("1"))
          .expectSuccessfulHandlerExecution()
          .expectEvents(projectCreatedEvent)
    }

    @Test
    fun `should throw exception if project already exists`() {
      val createProjectCommand =
          CreateProjectCommand(
              aggregateIdentifier = ProjectId("1"),
              projectName = "test",
              plannedStartDate = LocalDate.of(2021, 1, 1),
              deadline = LocalDate.of(2022, 1, 1),
          )
      val projectCreatedEvent =
          ProjectCreatedEvent(
              aggregateIdentifier = ProjectId("1"),
              projectName = "test",
              plannedStartDate = LocalDate.of(2021, 1, 1),
              deadline = LocalDate.of(2022, 1, 1),
          )

      fixture
          .given(projectCreatedEvent)
          .`when`(createProjectCommand)
          .expectException(AlreadyExistsException::class.java)
    }

    @Test
    fun `should throw exception if start date is after deadline`() {
      val createProjectCommand =
          CreateProjectCommand(
              aggregateIdentifier = ProjectId("1"),
              projectName = "test",
              plannedStartDate = LocalDate.of(2022, 1, 1),
              deadline = LocalDate.of(2021, 1, 1),
          )

      fixture
          .givenNoPriorActivity()
          .`when`(createProjectCommand)
          .expectException(IllegalArgumentException::class.java)
    }
  }

  @Nested
  inner class RenameProjectCommandTests {
    @Test
    fun `should rename project if new name is different to current`() {
      val projectCreatedEvent =
          ProjectCreatedEvent(
              aggregateIdentifier = ProjectId("1"),
              projectName = "test",
              plannedStartDate = LocalDate.of(2021, 1, 1),
              deadline = LocalDate.of(2022, 1, 1),
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

      fixture
          .given(projectCreatedEvent)
          .`when`(renameProjectCommand)
          .expectResultMessagePayload(1L)
          .expectSuccessfulHandlerExecution()
          .expectEvents(projectRenamedEvent)
    }

    @Test
    fun `should not rename project if new name is same as current`() {
      val projectCreatedEvent =
          ProjectCreatedEvent(
              aggregateIdentifier = ProjectId("1"),
              projectName = "test",
              plannedStartDate = LocalDate.of(2021, 1, 1),
              deadline = LocalDate.of(2022, 1, 1),
          )
      val renameProjectCommand =
          RenameProjectCommand(
              aggregateIdentifier = ProjectId("1"),
              aggregateVersion = 0,
              newName = "test",
          )

      fixture
          .given(projectCreatedEvent)
          .`when`(renameProjectCommand)
          .expectResultMessagePayload(0L)
          .expectSuccessfulHandlerExecution()
          .expectNoEvents()
    }

    @Test
    fun `should throw exception if renaming is based on wrong version`() {
      val projectCreatedEvent =
          ProjectCreatedEvent(
              aggregateIdentifier = ProjectId("1"),
              projectName = "test",
              plannedStartDate = LocalDate.of(2021, 1, 1),
              deadline = LocalDate.of(2022, 1, 1),
          )
      val projectRenamedEvent =
          ProjectRenamedEvent(
              aggregateIdentifier = ProjectId("1"),
              newName = "test2",
          )
      val renameProjectCommand =
          RenameProjectCommand(
              aggregateIdentifier = ProjectId("1"),
              aggregateVersion = 0,
              newName = "test3",
          )

      fixture
          .given(projectCreatedEvent, projectRenamedEvent)
          .`when`(renameProjectCommand)
          .expectException(ConflictingAggregateVersionException::class.java)
    }

    @Test
    fun `should not throw or rename if renaming is based on wrong version but new name is same as current`() {
      val projectCreatedEvent =
          ProjectCreatedEvent(
              aggregateIdentifier = ProjectId("1"),
              projectName = "test",
              plannedStartDate = LocalDate.of(2021, 1, 1),
              deadline = LocalDate.of(2022, 1, 1),
          )
      val projectRenamedEvent =
          ProjectRenamedEvent(
              aggregateIdentifier = ProjectId("1"),
              newName = "test2",
          )
      val projectRenamedEvent2 =
          ProjectRenamedEvent(
              aggregateIdentifier = ProjectId("1"),
              newName = "test3",
          )
      val renameProjectCommand =
          RenameProjectCommand(
              aggregateIdentifier = ProjectId("1"),
              aggregateVersion = 0,
              newName = "test3",
          )

      fixture
          .given(projectCreatedEvent, projectRenamedEvent, projectRenamedEvent2)
          .`when`(renameProjectCommand)
          .expectResultMessagePayload(2L)
          .expectSuccessfulHandlerExecution()
          .expectNoEvents()
    }
  }

  @Nested
  inner class RescheduleProjectCommandTests {
    @Test
    fun `should reschedule project if start date is different from current`() {
      val projectCreatedEvent =
          ProjectCreatedEvent(
              aggregateIdentifier = ProjectId("1"),
              projectName = "test",
              plannedStartDate = LocalDate.of(2021, 1, 1),
              deadline = LocalDate.of(2022, 1, 1),
          )
      val rescheduleProjectCommand =
          RescheduleProjectCommand(
              aggregateIdentifier = ProjectId("1"),
              aggregateVersion = 0,
              newStartDate = LocalDate.of(2020, 1, 1),
              newDeadline = LocalDate.of(2022, 1, 1),
          )
      val projectRescheduledEvent =
          ProjectRescheduledEvent(
              aggregateIdentifier = ProjectId("1"),
              newStartDate = LocalDate.of(2020, 1, 1),
              newDeadline = LocalDate.of(2022, 1, 1),
          )

      fixture
          .given(projectCreatedEvent)
          .`when`(rescheduleProjectCommand)
          .expectResultMessagePayload(1L)
          .expectSuccessfulHandlerExecution()
          .expectEvents(projectRescheduledEvent)
    }

    @Test
    fun `should reschedule project if deadline is different from current`() {
      val projectCreatedEvent =
          ProjectCreatedEvent(
              aggregateIdentifier = ProjectId("1"),
              projectName = "test",
              plannedStartDate = LocalDate.of(2021, 1, 1),
              deadline = LocalDate.of(2022, 1, 1),
          )
      val rescheduleProjectCommand =
          RescheduleProjectCommand(
              aggregateIdentifier = ProjectId("1"),
              aggregateVersion = 0,
              newStartDate = LocalDate.of(2021, 1, 1),
              newDeadline = LocalDate.of(2023, 1, 1),
          )
      val projectRescheduledEvent =
          ProjectRescheduledEvent(
              aggregateIdentifier = ProjectId("1"),
              newStartDate = LocalDate.of(2021, 1, 1),
              newDeadline = LocalDate.of(2023, 1, 1),
          )

      fixture
          .given(projectCreatedEvent)
          .`when`(rescheduleProjectCommand)
          .expectResultMessagePayload(1L)
          .expectSuccessfulHandlerExecution()
          .expectEvents(projectRescheduledEvent)
    }

    @Test
    fun `should not reschedule project if new start date and deadline is same as current`() {
      val projectCreatedEvent =
          ProjectCreatedEvent(
              aggregateIdentifier = ProjectId("1"),
              projectName = "test",
              plannedStartDate = LocalDate.of(2021, 1, 1),
              deadline = LocalDate.of(2022, 1, 1),
          )
      val rescheduleProjectCommand =
          RescheduleProjectCommand(
              aggregateIdentifier = ProjectId("1"),
              aggregateVersion = 0,
              newStartDate = LocalDate.of(2021, 1, 1),
              newDeadline = LocalDate.of(2022, 1, 1),
          )

      fixture
          .given(projectCreatedEvent)
          .`when`(rescheduleProjectCommand)
          .expectResultMessagePayload(0L)
          .expectSuccessfulHandlerExecution()
          .expectNoEvents()
    }

    @Test
    fun `should throw exception if rescheduling is based on wrong version`() {
      val projectCreatedEvent =
          ProjectCreatedEvent(
              aggregateIdentifier = ProjectId("1"),
              projectName = "test",
              plannedStartDate = LocalDate.of(2021, 1, 1),
              deadline = LocalDate.of(2022, 1, 1),
          )
      val projectRescheduledEvent =
          ProjectRescheduledEvent(
              aggregateIdentifier = ProjectId("1"),
              newStartDate = LocalDate.of(2021, 1, 1),
              newDeadline = LocalDate.of(2023, 1, 1),
          )
      val rescheduleProjectCommand =
          RescheduleProjectCommand(
              aggregateIdentifier = ProjectId("1"),
              aggregateVersion = 0,
              newStartDate = LocalDate.of(2021, 1, 1),
              newDeadline = LocalDate.of(2024, 1, 1),
          )

      fixture
          .given(projectCreatedEvent, projectRescheduledEvent)
          .`when`(rescheduleProjectCommand)
          .expectException(ConflictingAggregateVersionException::class.java)
    }

    @Test
    fun `should not throw or reschedule if renaming is based on wrong version but new dates are same as current`() {
      val projectCreatedEvent =
          ProjectCreatedEvent(
              aggregateIdentifier = ProjectId("1"),
              projectName = "test",
              plannedStartDate = LocalDate.of(2021, 1, 1),
              deadline = LocalDate.of(2022, 1, 1),
          )
      val projectRescheduledEvent =
          ProjectRescheduledEvent(
              aggregateIdentifier = ProjectId("1"),
              newStartDate = LocalDate.of(2021, 1, 1),
              newDeadline = LocalDate.of(2023, 1, 1),
          )
      val projectRescheduledEvent2 =
          ProjectRescheduledEvent(
              aggregateIdentifier = ProjectId("1"),
              newStartDate = LocalDate.of(2021, 1, 1),
              newDeadline = LocalDate.of(2024, 1, 1),
          )
      val rescheduleProjectCommand =
          RescheduleProjectCommand(
              aggregateIdentifier = ProjectId("1"),
              aggregateVersion = 0,
              newStartDate = LocalDate.of(2021, 1, 1),
              newDeadline = LocalDate.of(2024, 1, 1),
          )

      fixture
          .given(projectCreatedEvent, projectRescheduledEvent, projectRescheduledEvent2)
          .`when`(rescheduleProjectCommand)
          .expectResultMessagePayload(2L)
          .expectSuccessfulHandlerExecution()
          .expectNoEvents()
    }
  }
}
