package com.novatecgmbh.eventsourcing.axon.project.authorization

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository interface ProjectAclRepository : JpaRepository<ProjectAcl, ProjectAclKey>
