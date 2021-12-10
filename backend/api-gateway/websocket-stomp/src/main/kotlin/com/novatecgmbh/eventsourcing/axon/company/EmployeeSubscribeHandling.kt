package com.novatecgmbh.eventsourcing.axon.company

import com.novatecgmbh.eventsourcing.axon.application.security.RegisteredUserPrincipal
import com.novatecgmbh.eventsourcing.axon.application.websocket.BaseSubscribeHandling
import com.novatecgmbh.eventsourcing.axon.application.websocket.CurrentUserSubscriptionQueries
import com.novatecgmbh.eventsourcing.axon.company.company.api.CompanyId
import com.novatecgmbh.eventsourcing.axon.company.employee.api.EmployeeId
import com.novatecgmbh.eventsourcing.axon.company.employee.api.EmployeeQuery
import com.novatecgmbh.eventsourcing.axon.company.employee.api.EmployeeQueryResult
import com.novatecgmbh.eventsourcing.axon.company.employee.api.EmployeesByCompanyQuery
import org.axonframework.messaging.responsetypes.ResponseTypes
import org.axonframework.queryhandling.QueryGateway
import org.axonframework.queryhandling.SubscriptionQueryResult
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Component
import org.springframework.util.AntPathMatcher

@Component
class EmployeeSubscribeHandling(
    currentUserSubscriptions: CurrentUserSubscriptionQueries,
    messagingTemplate: SimpMessagingTemplate,
    val queryGateway: QueryGateway,
) : BaseSubscribeHandling(currentUserSubscriptions, messagingTemplate) {
  override fun mapToSubscriptionQuery(
      destination: String,
      user: RegisteredUserPrincipal
  ): SubscriptionQueryResult<*, *>? {
    val pathMatcher = AntPathMatcher()
    val parts = destination.substring(1).split("/")
    return when {
      pathMatcher.match("/companies/{companyId}/employees", destination) ->
          queryGateway.subscriptionQuery(
              EmployeesByCompanyQuery(CompanyId(parts.component2())),
              ResponseTypes.multipleInstancesOf(EmployeeQueryResult::class.java),
              ResponseTypes.instanceOf(EmployeeQueryResult::class.java),
          )
      pathMatcher.match("/employees/{employeeId}", destination) ->
          queryGateway.subscriptionQuery(
              EmployeeQuery(EmployeeId(parts.component2())),
              ResponseTypes.instanceOf(EmployeeQueryResult::class.java),
              ResponseTypes.instanceOf(EmployeeQueryResult::class.java),
          )
      else -> null
    }
  }
}
