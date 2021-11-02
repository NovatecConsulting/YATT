package com.novatecgmbh.eventsourcing.axon.project.authorization.idmapping

import com.novatecgmbh.eventsourcing.axon.project.ProjectContextAggregateTypesEnum
import com.novatecgmbh.eventsourcing.axon.project.ProjectContextAggregateTypesEnum.PARTICIPANT
import com.novatecgmbh.eventsourcing.axon.project.ProjectContextAggregateTypesEnum.TASK
import com.novatecgmbh.eventsourcing.axon.project.participant.api.ParticipantCreatedEvent
import com.novatecgmbh.eventsourcing.axon.project.project.api.ProjectId
import com.novatecgmbh.eventsourcing.axon.project.task.api.TaskCreatedEvent
import org.axonframework.config.ProcessingGroup
import org.axonframework.eventhandling.EventHandler
import org.axonframework.eventhandling.ResetHandler
import org.axonframework.queryhandling.QueryGateway
import org.springframework.stereotype.Component

@Component
@ProcessingGroup("project-acl-id-mapping-projector")
class ProjectAclIdMappingProjector(
    val queryGateway: QueryGateway,
    val repository: ProjectAclIdMappingRepository
) {

  @EventHandler
  fun on(event: TaskCreatedEvent) = addIdMapping(TASK, event.identifier.toString(), event.projectId)

  @EventHandler
  fun on(event: ParticipantCreatedEvent) =
      addIdMapping(PARTICIPANT, event.aggregateIdentifier.toString(), event.projectId)

  fun addIdMapping(
      type: ProjectContextAggregateTypesEnum,
      identifier: String,
      projectId: ProjectId
  ) = repository.save(ProjectAclIdMapping(ProjectAclIdMappingKey(type, identifier, projectId)))

  @ResetHandler
  fun reset() {
    repository.deleteAll()
  }
}
