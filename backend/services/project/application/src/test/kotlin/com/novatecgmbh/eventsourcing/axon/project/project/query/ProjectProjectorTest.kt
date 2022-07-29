package com.novatecgmbh.eventsourcing.axon.project.project.query

import com.novatecgmbh.eventsourcing.axon.common.api.AggregateReference
import com.novatecgmbh.eventsourcing.axon.company.company.api.CompanyId
import com.novatecgmbh.eventsourcing.axon.company.company.api.CompanyQuery
import com.novatecgmbh.eventsourcing.axon.company.company.api.CompanyQueryResult
import com.novatecgmbh.eventsourcing.axon.project.authorization.acl.ProjectAclRepository
import com.novatecgmbh.eventsourcing.axon.project.project.api.*
import com.novatecgmbh.eventsourcing.axon.project.project.api.ProjectStatus.ON_TIME
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.justRun
import io.mockk.slot
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
  @MockK private lateinit var lookupRepository: ProjectByTaskLookupProjectionRepository
  @MockK private lateinit var updateEmitter: QueryUpdateEmitter
  @MockK private lateinit var queryGateway: QueryGateway
  @MockK private lateinit var aclRepository: ProjectAclRepository

  private lateinit var testSubject: ProjectProjector

  private val companyQueryResult = CompanyQueryResult(CompanyId(), 0, "MyCompany")
  private var projectProjectionCaptor = slot<ProjectProjection>()
  private var projectQueryResultsCaptor = mutableListOf<ProjectQueryResult>()

  @BeforeEach
  fun setUp() {
    testSubject =
        ProjectProjector(repository, lookupRepository, aclRepository, updateEmitter, queryGateway)
    every { (repository.findById(ProjectId("1"))) } answers
        {
          Optional.of(
              ProjectProjection(
                  identifier = ProjectId("1"),
                  name = "test",
                  version = 0L,
                  plannedStartDate = LocalDate.of(2021, 1, 1),
                  deadline = LocalDate.of(2022, 1, 1),
                  companyReference = companyQueryResult.toAggregateReference(),
                  status = ON_TIME,
                  actualEndDate = null,
                  allTasksCount = 0,
                  plannedTasksCount = 0,
                  startedTasksCount = 0,
                  completedTasksCount = 0,
              ))
        }
    every { repository.save(capture(projectProjectionCaptor)) } answers
        {
          args.first() as ProjectProjection
        }
    every { queryGateway.queryOptional<CompanyQueryResult, CompanyQuery>(any()).get() } returns
        Optional.of(companyQueryResult)

    justRun {
      updateEmitter.emit(ProjectQuery::class.java, any(), capture(projectQueryResultsCaptor))
    }
    justRun {
      updateEmitter.emit(ProjectDetailsQuery::class.java, any(), any<ProjectDetailsQueryResult>())
    }
    justRun {
      updateEmitter.emit(MyProjectsQuery::class.java, any(), capture(projectQueryResultsCaptor))
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
    val expectedStatus = ON_TIME

    val testEvent =
        ProjectCreatedEvent(
            expectedIdentifier,
            expectedName,
            expectedStartDate,
            expectedDeadline,
            expectedCompanyReference.identifier,
            expectedStatus)
    testSubject.on(testEvent, expectedVersion)

    `verify that save and emit are called with correct arguments`(
        expectedIdentifier,
        expectedName,
        expectedStartDate,
        expectedDeadline,
        expectedVersion,
        expectedCompanyReference,
        expectedStatus)
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

  private fun `verify that save and emit are called with correct arguments`(
      expectedIdentifier: ProjectId,
      expectedName: String?,
      expectedStartDate: LocalDate?,
      expectedDeadline: LocalDate?,
      expectedVersion: Long,
      expectedCompanyReference: AggregateReference<CompanyId>? = null,
      expectedStatus: ProjectStatus? = null
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
    if (expectedStatus != null)
        assertEquals(expectedStatus, projectProjectionCaptor.captured.status)

    assertNotNull(projectQueryResultsCaptor)
    assertEquals(2, projectQueryResultsCaptor.size)
    for (projectQueryResult in projectQueryResultsCaptor) {
      assertEquals(expectedIdentifier, projectQueryResult.identifier)
      if (expectedName != null) assertEquals(expectedName, projectQueryResult.name)
      if (expectedStartDate != null) assertEquals(expectedStartDate, projectQueryResult.startDate)
      if (expectedDeadline != null) assertEquals(expectedDeadline, projectQueryResult.deadline)
      assertEquals(expectedVersion, projectQueryResult.version)
      if (expectedCompanyReference != null)
          assertEquals(expectedCompanyReference, projectQueryResult.companyReference)
      if (expectedStatus != null) assertEquals(expectedStatus, projectQueryResult.status)
    }
  }
}
