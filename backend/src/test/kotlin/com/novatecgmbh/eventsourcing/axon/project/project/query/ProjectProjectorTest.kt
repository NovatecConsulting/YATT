package com.novatecgmbh.eventsourcing.axon.project.project.query

import com.novatecgmbh.eventsourcing.axon.project.project.api.*
import java.time.LocalDate
import java.util.*
import org.axonframework.queryhandling.QueryUpdateEmitter
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor
import org.mockito.Mockito.*

class ProjectProjectorTest {
  private val repository: ProjectProjectionRepository =
      mock(ProjectProjectionRepository::class.java)
  private val updateEmitter: QueryUpdateEmitter = mock(QueryUpdateEmitter::class.java)

  private lateinit var testSubject: ProjectProjector

  @BeforeEach
  fun setUp() {
    testSubject = ProjectProjector(repository, updateEmitter)
    `when`(repository.findById(ProjectId("1")))
        .thenReturn(
            Optional.of(
                ProjectProjection(
                    identifier = ProjectId("1"),
                    name = "test",
                    version = 0L,
                    plannedStartDate = LocalDate.of(2021, 1, 1),
                    deadline = LocalDate.of(2022, 1, 1),
                )))
  }

  @Test
  fun `on ProjectCreatedEvent should save entity in repository and emit update`() {
    val expectedIdentifier = ProjectId("1")
    val expectedName = "test"
    val expectedStartDate = LocalDate.of(2021, 1, 1)
    val expectedDeadline = LocalDate.of(2022, 1, 1)
    val expectedVersion = 0L

    val testEvent =
        ProjectCreatedEvent(
            expectedIdentifier,
            expectedName,
            expectedStartDate,
            expectedDeadline,
        )
    testSubject.on(testEvent, expectedVersion)

    val projectEntityCaptor = ArgumentCaptor.forClass(ProjectProjection::class.java)
    verify(repository).save(projectEntityCaptor.capture())
    verify(updateEmitter)
        .emit<ProjectQuery, ProjectProjection>(any(), any(), projectEntityCaptor.capture())

    val capturedResults = projectEntityCaptor.allValues
    for (capturedResult in capturedResults) {
      assertNotNull(capturedResult)
      assertEquals(expectedIdentifier, capturedResult.identifier)
      assertEquals(expectedName, capturedResult.name)
      assertEquals(expectedStartDate, capturedResult.plannedStartDate)
      assertEquals(expectedDeadline, capturedResult.deadline)
      assertEquals(expectedVersion, capturedResult.version)
    }
  }

  @Test
  fun `on ProjectRenamedEvent should update entity in repository and emit update`() {
    val expectedIdentifier = ProjectId("1")
    val expectedName = "new name"
    val expectedVersion = 1L

    val testEvent =
        ProjectRenamedEvent(
            expectedIdentifier,
            expectedName,
        )
    testSubject.on(testEvent, expectedVersion)

    val projectProjectionCaptor = ArgumentCaptor.forClass(ProjectProjection::class.java)
    verify(repository).findById(ProjectId("1"))
    verify(updateEmitter)
        .emit<ProjectQuery, ProjectProjection>(any(), any(), projectProjectionCaptor.capture())

    val capturedResults = projectProjectionCaptor.allValues
    for (capturedResult in capturedResults) {
      assertNotNull(capturedResult)
      assertEquals(expectedIdentifier, capturedResult.identifier)
      assertEquals(expectedName, capturedResult.name)
      assertEquals(expectedVersion, capturedResult.version)
    }
  }

  @Test
  fun `on ProjectRescheduledEvent should save entity in repository and emit update`() {
    val expectedIdentifier = ProjectId("1")
    val expectedStartDate = LocalDate.of(2021, 1, 1)
    val expectedDeadline = LocalDate.of(2022, 1, 1)
    val expectedVersion = 1L

    val testEvent =
        ProjectRescheduledEvent(
            expectedIdentifier,
            expectedStartDate,
            expectedDeadline,
        )
    testSubject.on(testEvent, expectedVersion)

    val projectProjectionCaptor = ArgumentCaptor.forClass(ProjectProjection::class.java)
    verify(repository).findById(ProjectId("1"))
    verify(updateEmitter)
        .emit<ProjectQuery, ProjectProjection>(any(), any(), projectProjectionCaptor.capture())

    val capturedResults = projectProjectionCaptor.allValues
    for (capturedResult in capturedResults) {
      assertNotNull(capturedResult)
      assertEquals(expectedIdentifier, capturedResult.identifier)
      assertEquals(expectedStartDate, capturedResult.plannedStartDate)
      assertEquals(expectedDeadline, capturedResult.deadline)
      assertEquals(expectedVersion, capturedResult.version)
    }
  }

  @Test
  fun `handle ProjectQuery should call repository findById`() {
    val testQuery = ProjectQuery(ProjectId("1"))
    testSubject.handle(testQuery)

    verify(repository).findById(ProjectId("1"))
  }

  @Test
  fun `handle AllProjectsQuery should call repository findAll`() {
    val testQuery = AllProjectsQuery()
    testSubject.handle(testQuery)

    verify(repository).findAll()
  }
}
