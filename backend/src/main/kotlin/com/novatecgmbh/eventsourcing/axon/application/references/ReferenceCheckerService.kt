package com.novatecgmbh.eventsourcing.axon.application.references

import com.novatecgmbh.eventsourcing.axon.company.company.api.CompanyId
import com.novatecgmbh.eventsourcing.axon.project.project.api.ProjectId
import com.novatecgmbh.eventsourcing.axon.user.api.UserId
import org.springframework.stereotype.Service

@Service
class ReferenceCheckerService(private val repository: RootContextIdMappingRepository) {
  fun assertProjectExists(projectId: ProjectId) {
    if (!repository.existsProjectById(projectId.toString())) {
      throw IllegalArgumentException("Referenced Project does not exist")
    }
  }

  fun assertCompanyExists(companyId: CompanyId) {
    if (!repository.existsCompanyById(companyId.toString())) {
      throw IllegalArgumentException("Referenced Company does not exist")
    }
  }

  fun assertUserExists(userId: UserId) {
    if (!repository.existsUserById(userId.toString())) {
      throw IllegalArgumentException("Referenced User does not exist")
    }
  }
}
