package com.novatecgmbh.eventsourcing.axon.project.project.query

import com.novatecgmbh.eventsourcing.axon.common.query.AggregateReference
import com.novatecgmbh.eventsourcing.axon.company.company.api.CompanyId
import com.novatecgmbh.eventsourcing.axon.company.company.api.CompanyQuery
import com.novatecgmbh.eventsourcing.axon.company.company.api.CompanyQueryResult
import com.novatecgmbh.eventsourcing.axon.project.project.api.*
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.justRun
import io.mockk.slot
import io.mockk.verify
import java.time.LocalDate
import java.util.*
import org.axonframework.extensions.kotlin.queryOptional
import org.axonframework.queryhandling.QueryGateway
import org.axonframework.queryhandling.QueryUpdateEmitter
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class ProjectProjectorTest {

  @MockK private lateinit var repository: ProjectProjectionRepository
  @MockK private lateinit var updateEmitter: QueryUpdateEmitter
  @MockK private lateinit var queryGateway: QueryGateway

  private lateinit var testSubject: ProjectProjector

  private val companyQueryResult = CompanyQueryResult(CompanyId(), 0, "MyCompany")
  private var projectProjectionCaptor = slot<ProjectProjection>()
  private var projectQueryResultCaptor = slot<ProjectQueryResult>()

  @BeforeEach
  fun setUp() {
    testSubject = ProjectProjector(repository, updateEmitter, queryGateway)
    every { (repository.findById(ProjectId("1"))) } answers
        {
          Optional.of(
              ProjectProjection(
                  identifier = ProjectId("1"),
                  name = "test",
                  version = 0L,
                  plannedStartDate = LocalDate.of(2021, 1, 1),
                  deadline = LocalDate.of(2022, 1, 1),
                  companyReference = companyQueryResult.toAggregateReference()))
        }
    every { repository.save(capture(projectProjectionCaptor)) } answers
        {
          args.first() as ProjectProjection
        }
    every { queryGateway.queryOptional<CompanyQueryResult, CompanyQuery>(any()).get() } returns
        Optional.of(companyQueryResult)

    justRun {
      updateEmitter.emit(ProjectQuery::class.java, any(), capture(projectQueryResultCaptor))
    }
    justRun {
      updateEmitter.emit(AllProjectsQuery::class.java, any(), capture(projectQueryResultCaptor))
    }
  }

  @Test
  fun `on ProjectCreatedEvent should save entity in repository and emit update`() {
    val expectedIdentifier = ProjectId("1")
    val expectedName = "test"
    val expectedStartDate = LocalDate.of(2021, 1, 1)
    val expectedDeadline = LocalDate.of(2022, 1, 1)
    val expectedVersion = 0L
    val expectedCompanyReference = companyQueryResult.toAggregateReference()

    val testEvent =
        ProjectCreatedEvent(
            expectedIdentifier,
            expectedName,
            expectedStartDate,
            expectedDeadline,
            expectedCompanyReference.identifier)
    testSubject.on(testEvent, expectedVersion)

    `verify that save and emit are called with correct arguments`(
        expectedIdentifier,
        expectedName,
        expectedStartDate,
        expectedDeadline,
        expectedVersion,
        expectedCompanyReference)
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
        expectedIdentifier, expectedName, null, null, expectedVersion)
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
        expectedIdentifier, null, expectedStartDate, expectedDeadline, expectedVersion)
  }

  @Test
  fun `handle ProjectQuery should call repository findById`() {
    testSubject.handle(ProjectQuery(ProjectId("1")))
    verify(exactly = 1) { repository.findById(ProjectId("1")) }
  }

  @Test
  fun `handle AllProjectsQuery should call repository findAll`() {
    every { repository.findAll() } returns emptyList()
    testSubject.handle(AllProjectsQuery())
    verify(exactly = 1) { repository.findAll() }
  }

  private fun `verify that save and emit are called with correct arguments`(
      expectedIdentifier: ProjectId,
      expectedName: String?,
      expectedStartDate: LocalDate?,
      expectedDeadline: LocalDate?,
      expectedVersion: Long,
      expectedCompanyReference: AggregateReference<CompanyId>? = null
  ) {
    assertNotNull(projectProjectionCaptor.captured)
    assertEquals(expectedIdentifier, projectProjectionCaptor.captured.identifier)
    if (expectedName != null) assertEquals(expectedName, projectProjectionCaptor.captured.name)
    if (expectedStartDate != null)
        assertEquals(expectedStartDate, projectProjectionCaptor.captured.plannedStartDate)
    if (expectedDeadline != null)
        assertEquals(expectedDeadline, projectProjectionCaptor.captured.deadline)
    assertEquals(expectedVersion, projectProjectionCaptor.captured.version)
    if (expectedCompanyReference != null)
        assertEquals(expectedCompanyReference, projectProjectionCaptor.captured.companyReference)

    assertNotNull(projectQueryResultCaptor.captured)
    assertEquals(expectedIdentifier, projectQueryResultCaptor.captured.identifier)
    if (expectedName != null) assertEquals(expectedName, projectQueryResultCaptor.captured.name)
    if (expectedStartDate != null)
        assertEquals(expectedStartDate, projectQueryResultCaptor.captured.startDate)
    if (expectedDeadline != null)
        assertEquals(expectedDeadline, projectQueryResultCaptor.captured.deadline)
    assertEquals(expectedVersion, projectQueryResultCaptor.captured.version)
    if (expectedCompanyReference != null)
        assertEquals(expectedCompanyReference, projectQueryResultCaptor.captured.companyReference)
  }
}
