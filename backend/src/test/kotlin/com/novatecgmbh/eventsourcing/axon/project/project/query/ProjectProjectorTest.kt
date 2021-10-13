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
    `when`(repository.save(any())).then { it.arguments.first() }
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

    `verify that save and emit are called with correct arguments`(
        expectedIdentifier,
        expectedName,
        expectedStartDate,
        expectedDeadline,
        expectedVersion,
    )
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

    `verify that save and emit are called with correct arguments`(
        expectedIdentifier,
        expectedName,
        null,
        null,
        expectedVersion,
    )
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

    `verify that save and emit are called with correct arguments`(
        expectedIdentifier,
        null,
        expectedStartDate,
        expectedDeadline,
        expectedVersion,
    )
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

  private fun `verify that save and emit are called with correct arguments`(
      expectedIdentifier: ProjectId,
      expectedName: String?,
      expectedStartDate: LocalDate?,
      expectedDeadline: LocalDate?,
      expectedVersion: Long,
  ) {
    val projectProjectionCaptor = ArgumentCaptor.forClass(ProjectProjection::class.java)
    val projectQueryResultCaptor = ArgumentCaptor.forClass(ProjectQueryResult::class.java)

    verify(repository).save(projectProjectionCaptor.capture())
    verify(updateEmitter)
        .emit(eq(ProjectQuery::class.java), any(), projectQueryResultCaptor.capture())
    verify(updateEmitter)
        .emit(eq(AllProjectsQuery::class.java), any(), projectQueryResultCaptor.capture())

    for (capturedResult in projectProjectionCaptor.allValues) {
      assertNotNull(capturedResult)
      assertEquals(expectedIdentifier, capturedResult.identifier)
      if (expectedName != null) assertEquals(expectedName, capturedResult.name)
      if (expectedStartDate != null)
          assertEquals(expectedStartDate, capturedResult.plannedStartDate)
      if (expectedDeadline != null) assertEquals(expectedDeadline, capturedResult.deadline)
      assertEquals(expectedVersion, capturedResult.version)
    }
    for (capturedResult in projectQueryResultCaptor.allValues) {
      assertNotNull(capturedResult)
      assertEquals(expectedIdentifier, capturedResult.identifier)
      if (expectedName != null) assertEquals(expectedName, capturedResult.name)
      if (expectedStartDate != null)
          assertEquals(expectedStartDate, capturedResult.startDate)
      if (expectedDeadline != null) assertEquals(expectedDeadline, capturedResult.deadline)
      assertEquals(expectedVersion, capturedResult.version)
    }
  }
}
