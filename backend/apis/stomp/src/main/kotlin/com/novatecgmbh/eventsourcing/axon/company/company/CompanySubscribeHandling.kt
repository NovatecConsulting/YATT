package com.novatecgmbh.eventsourcing.axon.company.company

import com.novatecgmbh.eventsourcing.axon.application.security.RegisteredUserPrincipal
import com.novatecgmbh.eventsourcing.axon.application.websocket.BaseSubscribeHandling
import com.novatecgmbh.eventsourcing.axon.application.websocket.CurrentUserSubscriptionQueries
import com.novatecgmbh.eventsourcing.axon.company.company.api.AllCompaniesQuery
import com.novatecgmbh.eventsourcing.axon.company.company.api.CompanyId
import com.novatecgmbh.eventsourcing.axon.company.company.api.CompanyQuery
import com.novatecgmbh.eventsourcing.axon.company.company.api.CompanyQueryResult
import org.axonframework.messaging.responsetypes.ResponseTypes
import org.axonframework.queryhandling.QueryGateway
import org.axonframework.queryhandling.SubscriptionQueryResult
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Component
import org.springframework.util.AntPathMatcher

@Component
class CompanySubscribeHandling(
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
      pathMatcher.match("/companies", destination) ->
          queryGateway.subscriptionQuery(
              AllCompaniesQuery(),
              ResponseTypes.multipleInstancesOf(CompanyQueryResult::class.java),
              ResponseTypes.instanceOf(CompanyQueryResult::class.java),
          )
      pathMatcher.match("/companies/{companyId}", destination) ->
          queryGateway.subscriptionQuery(
              CompanyQuery(CompanyId(parts.component2())),
              ResponseTypes.instanceOf(CompanyQueryResult::class.java),
              ResponseTypes.instanceOf(CompanyQueryResult::class.java),
          )
      else -> null
    }
  }
}
