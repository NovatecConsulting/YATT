package com.novatecgmbh.eventsourcing.axon.company.references

import org.springframework.stereotype.Service

@Service
class ReferenceCheckerService(private val repository: RootContextIdMappingRepository) {
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
