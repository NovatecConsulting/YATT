package com.novatecgmbh.eventsourcing.axon.project.participant.command.authorization

import com.novatecgmbh.eventsourcing.axon.application.auditing.AUDIT_USER_ID_META_DATA_KEY
import com.novatecgmbh.eventsourcing.axon.project.authorization.acl.ProjectAclRepository
import com.novatecgmbh.eventsourcing.axon.project.participant.api.CreateParticipantCommand
import com.novatecgmbh.eventsourcing.axon.project.participant.api.ParticipantCommand
import com.novatecgmbh.eventsourcing.axon.user.api.UserId
import javax.annotation.PostConstruct
import org.axonframework.commandhandling.CommandBus
import org.axonframework.commandhandling.CommandMessage
import org.axonframework.messaging.InterceptorChain
import org.axonframework.messaging.MessageHandlerInterceptor
import org.axonframework.messaging.unitofwork.UnitOfWork
import org.springframework.stereotype.Component

@Component
class ParticipantAuthorizer(
    val projectAclRepository: ProjectAclRepository,
    val commandBus: CommandBus
) : MessageHandlerInterceptor<CommandMessage<*>> {

  @PostConstruct
  fun register() {
    commandBus.registerHandlerInterceptor(this as MessageHandlerInterceptor<in CommandMessage<*>>)
  }

  override fun handle(
      unitOfWork: UnitOfWork<out CommandMessage<*>>,
      interceptorChain: InterceptorChain
  ): Any? {
    val payload = unitOfWork.message.payload
    val userId = UserId(unitOfWork.message.metaData[AUDIT_USER_ID_META_DATA_KEY].toString())
    if (payload is ParticipantCommand) {
      when (payload) {
        is CreateParticipantCommand -> authorize(payload, userId)
        else -> throw IllegalStateException("Authorization rule missing for participant command")
      }
    }
    return interceptorChain.proceed()
  }

  fun authorize(command: CreateParticipantCommand, userId: UserId) {
    if (!projectAclRepository.hasAccessToProject(userId, command.projectId.toString())) {
      throw IllegalAccessException("Not authorized to add participant to this project")
    }
  }
}
