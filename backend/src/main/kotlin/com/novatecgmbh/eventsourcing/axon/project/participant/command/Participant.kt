package com.novatecgmbh.eventsourcing.axon.project.participant.command

import com.novatecgmbh.eventsourcing.axon.common.command.AlreadyExistsException
import com.novatecgmbh.eventsourcing.axon.company.company.api.CompanyId
import com.novatecgmbh.eventsourcing.axon.company.company.command.Company
import com.novatecgmbh.eventsourcing.axon.project.participant.api.CreateParticipantCommand
import com.novatecgmbh.eventsourcing.axon.project.participant.api.ParticipantCreatedEvent
import com.novatecgmbh.eventsourcing.axon.project.participant.api.ParticipantId
import com.novatecgmbh.eventsourcing.axon.project.project.api.ProjectId
import com.novatecgmbh.eventsourcing.axon.user.api.UserId
import com.novatecgmbh.eventsourcing.axon.user.command.User
import org.axonframework.commandhandling.CommandHandler
import org.axonframework.eventsourcing.EventSourcingHandler
import org.axonframework.modelling.command.*
import org.axonframework.spring.stereotype.Aggregate
import org.springframework.beans.factory.annotation.Autowired

@Aggregate
class Participant {
  @AggregateIdentifier private lateinit var aggregateIdentifier: ParticipantId
  private lateinit var projectId: ProjectId
  private lateinit var userId: UserId

  @CommandHandler
  @CreationPolicy(AggregateCreationPolicy.CREATE_IF_MISSING)
  fun handle(
      command: CreateParticipantCommand,
      @Autowired userRepository: Repository<User>,
      @Autowired companyRepository: Repository<Company>,
      @Autowired participantUniqueKeyRepository: ParticipantUniqueKeyRepository
  ): ParticipantId {
    if (::aggregateIdentifier.isInitialized) {
      throw AlreadyExistsException()
    }
    assertNoParticipantExistsForCompanyAndUser(
        participantUniqueKeyRepository, command.companyId, command.userId)
    assertUserExists(userRepository, command.userId)
    assertCompanyExists(companyRepository, command.companyId)
    AggregateLifecycle.apply(
        ParticipantCreatedEvent(
            aggregateIdentifier = command.aggregateIdentifier,
            projectId = command.projectId,
            companyId = command.companyId,
            userId = command.userId))
    return command.aggregateIdentifier
  }

  private fun assertUserExists(userRepository: Repository<User>, userId: UserId) {
    try {
      userRepository.load(userId.identifier)
    } catch (ex: AggregateNotFoundException) {
      throw IllegalArgumentException("Referenced User does not exist")
    }
  }

  private fun assertCompanyExists(companyRepository: Repository<Company>, companyId: CompanyId) {
    try {
      companyRepository.load(companyId.identifier)
    } catch (ex: AggregateNotFoundException) {
      throw IllegalArgumentException("Referenced Company does not exist")
    }
  }

  private fun assertNoParticipantExistsForCompanyAndUser(
      participantUniqueKeyRepository: ParticipantUniqueKeyRepository,
      companyId: CompanyId,
      userId: UserId
  ) {
    if (participantUniqueKeyRepository.existsByCompanyIdAndUserId(companyId, userId))
        throw IllegalArgumentException("Participant already exists for this company and user")
  }

  @EventSourcingHandler
  fun on(event: ParticipantCreatedEvent) {
    aggregateIdentifier = event.aggregateIdentifier
    projectId = event.projectId
    userId = event.userId
  }
}
