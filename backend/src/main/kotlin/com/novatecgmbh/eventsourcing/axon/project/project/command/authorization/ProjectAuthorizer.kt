package com.novatecgmbh.eventsourcing.axon.project.project.command.authorization

import com.novatecgmbh.eventsourcing.axon.application.auditing.AUDIT_USER_ID_META_DATA_KEY
import com.novatecgmbh.eventsourcing.axon.project.authorization.acl.ProjectAclRepository
import com.novatecgmbh.eventsourcing.axon.project.project.api.*
import com.novatecgmbh.eventsourcing.axon.user.api.UserId
import javax.annotation.PostConstruct
import org.axonframework.commandhandling.CommandBus
import org.axonframework.commandhandling.CommandMessage
import org.axonframework.messaging.InterceptorChain
import org.axonframework.messaging.MessageHandlerInterceptor
import org.axonframework.messaging.unitofwork.UnitOfWork
import org.springframework.stereotype.Component

@Component
class ProjectAuthorizer(
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
    if (payload is ProjectCommand) {
      when (payload) {
        is CreateProjectCommand -> authorize(payload, userId)
        is UpdateProjectCommand -> authorize(payload, userId)
        is RescheduleProjectCommand -> authorize(payload, userId)
        is RenameProjectCommand -> authorize(payload, userId)
        else -> IllegalStateException("Authorization rule missing for project command")
      }
    }
    return interceptorChain.proceed()
  }

  fun authorize(command: CreateProjectCommand, userId: UserId) {
    if (!projectAclRepository.hasPermissionToCreateProjectForCompany(
        userId, command.companyId.identifier)) {
      throw IllegalAccessException("Not authorized to create project for this company")
    }
  }

  fun authorize(command: UpdateProjectCommand, userId: UserId) {
    if (!projectAclRepository.hasAccessToProject(userId, command.aggregateIdentifier.toString())) {
      throw IllegalAccessException("Not authorized to update project")
    }
  }

  fun authorize(command: RescheduleProjectCommand, userId: UserId) {
    if (!projectAclRepository.hasAccessToProject(userId, command.aggregateIdentifier.toString())) {
      throw IllegalAccessException("Not authorized to reschedule project")
    }
  }

  fun authorize(command: RenameProjectCommand, userId: UserId) {
    if (!projectAclRepository.hasAccessToProject(userId, command.aggregateIdentifier.toString())) {
      throw IllegalAccessException("Not authorized to rename project")
    }
  }
}
