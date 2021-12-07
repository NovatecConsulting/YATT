package com.novatecgmbh.eventsourcing.axon.application.websocket

import com.novatecgmbh.eventsourcing.axon.application.security.RegisteredUserPrincipal
import com.novatecgmbh.eventsourcing.axon.application.security.SecurityContextHelper
import com.novatecgmbh.eventsourcing.axon.company.company.api.AllCompaniesQuery
import com.novatecgmbh.eventsourcing.axon.company.company.api.CompanyId
import com.novatecgmbh.eventsourcing.axon.company.company.api.CompanyQuery
import com.novatecgmbh.eventsourcing.axon.company.company.api.CompanyQueryResult
import com.novatecgmbh.eventsourcing.axon.company.employee.api.EmployeeId
import com.novatecgmbh.eventsourcing.axon.company.employee.api.EmployeeQuery
import com.novatecgmbh.eventsourcing.axon.company.employee.api.EmployeeQueryResult
import com.novatecgmbh.eventsourcing.axon.company.employee.api.EmployeesByCompanyQuery
import com.novatecgmbh.eventsourcing.axon.project.participant.api.ParticipantByProjectQuery
import com.novatecgmbh.eventsourcing.axon.project.participant.api.ParticipantId
import com.novatecgmbh.eventsourcing.axon.project.participant.api.ParticipantQuery
import com.novatecgmbh.eventsourcing.axon.project.participant.api.ParticipantQueryResult
import com.novatecgmbh.eventsourcing.axon.project.project.api.*
import com.novatecgmbh.eventsourcing.axon.project.task.api.TaskId
import com.novatecgmbh.eventsourcing.axon.project.task.api.TaskQuery
import com.novatecgmbh.eventsourcing.axon.project.task.api.TaskQueryResult
import com.novatecgmbh.eventsourcing.axon.project.task.api.TasksByProjectQuery
import org.axonframework.messaging.responsetypes.ResponseTypes
import org.axonframework.queryhandling.QueryGateway
import org.axonframework.queryhandling.SubscriptionQueryResult
import org.springframework.context.ApplicationListener
import org.springframework.messaging.simp.SimpMessageHeaderAccessor
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.stereotype.Component
import org.springframework.web.socket.messaging.SessionSubscribeEvent

@Component
class SubscribeHandling(
    val currentUserSubscriptions: CurrentUserSubscriptionQueries,
    val messagingTemplate: SimpMessagingTemplate,
    val queryGateway: QueryGateway,
) : ApplicationListener<SessionSubscribeEvent> {

  override fun onApplicationEvent(event: SessionSubscribeEvent) {
    val user = getRegisteredUser(event)

    val headerAccessor: SimpMessageHeaderAccessor =
        SimpMessageHeaderAccessor.getAccessor(event.message)!! as SimpMessageHeaderAccessor
    val sessionId = headerAccessor.sessionId
    val subscriptionId = headerAccessor.subscriptionId
    val destination = headerAccessor.destination

    if (subscriptionId != null && destination != null) {
      SecurityContextHelper.setAuthentication(user.identifier.toString())
      val paths = destination.split("/").drop(3) // drop empty string, /user, /topic

      val subscriptionQuery: SubscriptionQueryResult<*, *>? =
          when (paths.getOrNull(0)) {
            "projects" ->
                when (val id = paths.getOrNull(1)) {
                  null ->
                      queryGateway.subscriptionQuery(
                          MyProjectsQuery(user.identifier),
                          ResponseTypes.multipleInstancesOf(ProjectQueryResult::class.java),
                          ResponseTypes.instanceOf(ProjectQueryResult::class.java),
                      )
                  else ->
                      when (paths.getOrNull(2)) {
                        null ->
                            queryGateway.subscriptionQuery(
                                ProjectQuery(ProjectId(id)),
                                ResponseTypes.instanceOf(ProjectQueryResult::class.java),
                                ResponseTypes.instanceOf(ProjectQueryResult::class.java),
                            )
                        "tasks" ->
                            queryGateway.subscriptionQuery(
                                TasksByProjectQuery(ProjectId(id)),
                                ResponseTypes.multipleInstancesOf(TaskQueryResult::class.java),
                                ResponseTypes.instanceOf(TaskQueryResult::class.java),
                            )
                        "details" ->
                            queryGateway.subscriptionQuery(
                                ProjectDetailsQuery(ProjectId(id)),
                                ResponseTypes.instanceOf(ProjectQueryResult::class.java),
                                ResponseTypes.instanceOf(ProjectQueryResult::class.java),
                            )
                        "participants" ->
                            queryGateway.subscriptionQuery(
                                ParticipantByProjectQuery(ProjectId(id)),
                                ResponseTypes.multipleInstancesOf(
                                    ParticipantQueryResult::class.java),
                                ResponseTypes.instanceOf(ParticipantQueryResult::class.java),
                            )
                        else -> null
                      }
                }
            "participants" ->
                when (val id = paths.getOrNull(1)) {
                  null -> null
                  else ->
                      queryGateway.subscriptionQuery(
                          ParticipantQuery(ParticipantId(id)),
                          ResponseTypes.instanceOf(ParticipantQueryResult::class.java),
                          ResponseTypes.instanceOf(ParticipantQueryResult::class.java),
                      )
                }
            "tasks" ->
                when (val id = paths.getOrNull(1)) {
                  null -> null
                  else ->
                      queryGateway.subscriptionQuery(
                          TaskQuery(TaskId(id)),
                          ResponseTypes.instanceOf(TaskQueryResult::class.java),
                          ResponseTypes.instanceOf(TaskQueryResult::class.java),
                      )
                }
            "companies" ->
                when (val id = paths.getOrNull(1)) {
                  null ->
                      queryGateway.subscriptionQuery(
                          AllCompaniesQuery(),
                          ResponseTypes.multipleInstancesOf(CompanyQueryResult::class.java),
                          ResponseTypes.instanceOf(CompanyQueryResult::class.java),
                      )
                  else ->
                      when (paths.getOrNull(2)) {
                        null ->
                            queryGateway.subscriptionQuery(
                                CompanyQuery(CompanyId(id)),
                                ResponseTypes.instanceOf(CompanyQueryResult::class.java),
                                ResponseTypes.instanceOf(CompanyQueryResult::class.java),
                            )
                        "employees" ->
                            queryGateway.subscriptionQuery(
                                EmployeesByCompanyQuery(CompanyId(id)),
                                ResponseTypes.multipleInstancesOf(EmployeeQueryResult::class.java),
                                ResponseTypes.instanceOf(EmployeeQueryResult::class.java),
                            )
                        else -> null
                      }
                }
            "employees" ->
                when (val id = paths.getOrNull(1)) {
                  null -> null
                  else ->
                      queryGateway.subscriptionQuery(
                          EmployeeQuery(EmployeeId(id)),
                          ResponseTypes.instanceOf(EmployeeQueryResult::class.java),
                          ResponseTypes.instanceOf(EmployeeQueryResult::class.java),
                      )
                }
            else -> null
          }

      if (subscriptionQuery != null) {
        currentUserSubscriptions.add(
            subscriptionId,
            subscriptionQuery.apply {
              updates()
                  .doOnNext {
                    messagingTemplate.convertAndSendToUser(
                        user.externalUserId,
                        "/topic/${paths.joinToString("/")}",
                        it,
                        SimpMessageHeaderAccessor.create()
                            .apply { setSessionId(sessionId) }
                            .messageHeaders,
                    )
                  }
                  .subscribe()
            })
      } else {
        println("not found")
        // TODO error handling not found
      }
    }
  }

  private fun getRegisteredUser(event: SessionSubscribeEvent) =
      event.user.let { user ->
        if (user is UsernamePasswordAuthenticationToken) {
          val principal = user.principal
          if (principal is RegisteredUserPrincipal) {
            principal
          } else {
            // TODO
            throw RuntimeException("principal is not RegisteredUserPrincipal")
          }
        } else {
          // TODO
          throw RuntimeException("user is not UsernamePasswordAuthenticationToken")
        }
      }
}
