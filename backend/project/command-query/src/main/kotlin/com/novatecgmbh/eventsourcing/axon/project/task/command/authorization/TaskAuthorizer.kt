package com.novatecgmbh.eventsourcing.axon.project.task.command.authorization

import com.novatecgmbh.eventsourcing.axon.application.auditing.AUDIT_USER_ID_META_DATA_KEY
import com.novatecgmbh.eventsourcing.axon.project.authorization.acl.ProjectAclRepository
import com.novatecgmbh.eventsourcing.axon.project.references.RootContextIdMappingRepository
import com.novatecgmbh.eventsourcing.axon.project.task.api.*
import com.novatecgmbh.eventsourcing.axon.user.api.UserId
import javax.annotation.PostConstruct
import org.axonframework.commandhandling.CommandBus
import org.axonframework.commandhandling.CommandMessage
import org.axonframework.messaging.InterceptorChain
import org.axonframework.messaging.MessageHandlerInterceptor
import org.axonframework.messaging.unitofwork.UnitOfWork
import org.springframework.stereotype.Component

@Component
class TaskAuthorizer(
    val projectAclRepository: ProjectAclRepository,
    val rootContextIdMappingRepository: RootContextIdMappingRepository,
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
    if (payload is TaskCommand) {
      when (payload) {
        is CreateTaskCommand -> authorize(payload, userId)
        is RescheduleTaskCommand -> authorize(payload, userId)
        is RenameTaskCommand -> authorize(payload, userId)
        is ChangeTaskDescriptionCommand -> authorize(payload, userId)
        is StartTaskCommand -> authorize(payload, userId)
        is CompleteTaskCommand -> authorize(payload, userId)
        is AddTodoCommand -> authorize(payload, userId)
        is MarkTodoAsDoneCommand -> authorize(payload, userId)
        else -> throw IllegalStateException("Authorization rule missing for command")
      }
    }
    return interceptorChain.proceed()
  }

  fun authorize(command: CreateTaskCommand, userId: UserId) {
    if (!projectAclRepository.hasAccessToProject(userId, command.projectId.identifier)) {
      throw IllegalAccessException("Not authorized to create task in this project")
    }
  }

  fun authorize(command: RescheduleTaskCommand, userId: UserId) {
    if (!projectAclRepository.hasAccessToProject(
        userId,
        rootContextIdMappingRepository.findProjectIdByTaskId(command.identifier.toString()))) {
      throw IllegalAccessException("Not authorized to reschedule task in this project")
    }
  }

  fun authorize(command: RenameTaskCommand, userId: UserId) {
    if (!projectAclRepository.hasAccessToProject(
        userId,
        rootContextIdMappingRepository.findProjectIdByTaskId(command.identifier.toString()))) {
      throw IllegalAccessException("Not authorized to rename task in this project")
    }
  }

  fun authorize(command: ChangeTaskDescriptionCommand, userId: UserId) {
    if (!projectAclRepository.hasAccessToProject(
        userId,
        rootContextIdMappingRepository.findProjectIdByTaskId(command.identifier.toString()))) {
      throw IllegalAccessException("Not authorized to change task description in this project")
    }
  }

  fun authorize(command: StartTaskCommand, userId: UserId) {
    if (!projectAclRepository.hasAccessToProject(
        userId,
        rootContextIdMappingRepository.findProjectIdByTaskId(command.identifier.toString()))) {
      throw IllegalAccessException("Not authorized to start task in this project")
    }
  }

  fun authorize(command: CompleteTaskCommand, userId: UserId) {
    if (!projectAclRepository.hasAccessToProject(
        userId,
        rootContextIdMappingRepository.findProjectIdByTaskId(command.identifier.toString()))) {
      throw IllegalAccessException("Not authorized to complete task in this project")
    }
  }

  fun authorize(command: AddTodoCommand, userId: UserId) {
    if (!projectAclRepository.hasAccessToProject(
        userId,
        rootContextIdMappingRepository.findProjectIdByTaskId(command.identifier.toString()))) {
      throw IllegalAccessException("Not authorized to add todos to tasks in this project")
    }
  }

  fun authorize(command: MarkTodoAsDoneCommand, userId: UserId) {
    if (!projectAclRepository.hasAccessToProject(
        userId,
        rootContextIdMappingRepository.findProjectIdByTaskId(command.identifier.toString()))) {
      throw IllegalAccessException("Not authorized to mark todos as done in this project")
    }
  }
}
