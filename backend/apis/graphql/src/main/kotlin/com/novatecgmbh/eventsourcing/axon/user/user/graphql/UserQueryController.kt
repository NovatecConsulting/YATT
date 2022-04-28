package com.novatecgmbh.eventsourcing.axon.user.user.graphql

import com.novatecgmbh.eventsourcing.axon.company.employee.api.EmployeeQueryResult
import com.novatecgmbh.eventsourcing.axon.project.participant.api.ParticipantQueryResult
import com.novatecgmbh.eventsourcing.axon.user.api.AllUsersQuery
import com.novatecgmbh.eventsourcing.axon.user.api.UserQuery
import com.novatecgmbh.eventsourcing.axon.user.api.UserQueryResult
import java.util.concurrent.CompletableFuture
import org.axonframework.extensions.kotlin.query
import org.axonframework.extensions.kotlin.queryMany
import org.axonframework.queryhandling.QueryGateway
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.graphql.data.method.annotation.SchemaMapping
import org.springframework.stereotype.Controller

@Controller
class UserQueryController(val queryGateway: QueryGateway) {

  @QueryMapping
  fun users(): CompletableFuture<List<UserQueryResult>> = queryGateway.queryMany(AllUsersQuery())

  // TODO: Change to BatchMapping to be more efficient
  @SchemaMapping(typeName = "Participant")
  fun user(participant: ParticipantQueryResult): CompletableFuture<UserQueryResult> =
      queryGateway.query(UserQuery(participant.userId))

  // TODO: Change to BatchMapping to be more efficient
  @SchemaMapping(typeName = "Employee")
  fun user(employee: EmployeeQueryResult): CompletableFuture<UserQueryResult> =
      queryGateway.query(UserQuery(employee.userId))
}
