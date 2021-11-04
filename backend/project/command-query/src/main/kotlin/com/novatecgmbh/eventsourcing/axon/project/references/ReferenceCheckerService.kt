package com.novatecgmbh.eventsourcing.axon.project.references

import org.springframework.stereotype.Service

@Service
class ReferenceCheckerService(private val repository: RootContextIdMappingRepository) {
  fun assertProjectExists(projectId: String) {
    if (!repository.existsProjectById(projectId)) {
      throw IllegalArgumentException("Referenced Project does not exist")
    }
  }

  fun assertCompanyExists(companyId: String) {
    if (!repository.existsCompanyById(companyId)) {
      throw IllegalArgumentException("Referenced Company does not exist")
    }
  }

  fun assertUserExists(userId: String) {
    if (!repository.existsUserById(userId)) {
      throw IllegalArgumentException("Referenced User does not exist")
    }
  }
}
