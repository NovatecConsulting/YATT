package com.novatecgmbh.eventsourcing.axon.project.project.command

import com.novatecgmbh.eventsourcing.axon.application.auditing.AUDIT_USER_ID_META_DATA_KEY
import com.novatecgmbh.eventsourcing.axon.project.authorization.AuthorizableAggregateTypesEnum.COMPANY
import com.novatecgmbh.eventsourcing.axon.project.authorization.PermissionEnum.CREATE_PROJECT
import com.novatecgmbh.eventsourcing.axon.project.authorization.ProjectAclKey
import com.novatecgmbh.eventsourcing.axon.project.authorization.ProjectAclRepository
import com.novatecgmbh.eventsourcing.axon.project.project.api.CreateProjectCommand
import com.novatecgmbh.eventsourcing.axon.user.api.UserId
import javax.annotation.PostConstruct
import org.axonframework.commandhandling.CommandBus
import org.axonframework.commandhandling.CommandMessage
import org.axonframework.messaging.InterceptorChain
import org.axonframework.messaging.MessageHandlerInterceptor
import org.axonframework.messaging.unitofwork.UnitOfWork
import org.springframework.stereotype.Component

@Component
class CreateProjectAuthorizer(
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
  ) {

    val payload = unitOfWork.message.payload
    val userId = UserId(unitOfWork.message.metaData[AUDIT_USER_ID_META_DATA_KEY].toString())
    when (payload) {
      is CreateProjectCommand -> authorize(payload, userId)
      else -> interceptorChain.proceed()
    }
  }

  fun authorize(command: CreateProjectCommand, userId: UserId) {
    if (!projectAclRepository.existsById(
        ProjectAclKey(COMPANY, command.companyId.identifier, userId, CREATE_PROJECT))) {
      throw IllegalAccessException("Not authorized to create project for this company")
    }
  }
}
