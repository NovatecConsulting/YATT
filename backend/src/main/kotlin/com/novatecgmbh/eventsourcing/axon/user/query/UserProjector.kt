package com.novatecgmbh.eventsourcing.axon.user.query

import com.novatecgmbh.eventsourcing.axon.user.api.*
import java.util.*
import org.axonframework.config.ProcessingGroup
import org.axonframework.eventhandling.EventHandler
import org.axonframework.eventhandling.ResetHandler
import org.axonframework.extensions.kotlin.emit
import org.axonframework.queryhandling.QueryHandler
import org.axonframework.queryhandling.QueryUpdateEmitter
import org.springframework.stereotype.Component

@Component
@ProcessingGroup("user-projector")
class UserProjector(
    private val repository: UserProjectionRepository,
    private val queryUpdateEmitter: QueryUpdateEmitter
) {
  @EventHandler
  fun on(event: UserRegisteredEvent) {
    saveProjection(
        UserProjection(
            identifier = event.aggregateIdentifier,
            externalUserId = event.externalUserId,
            firstname = event.firstname,
            lastname = event.lastname))
  }

  @EventHandler
  fun on(event: UserRenamedEvent) {
    updateProjection(event.aggregateIdentifier) {
      it.firstname = event.firstname
      it.lastname = event.lastname
    }
  }

  private fun updateProjection(identifier: UserId, stateChanges: (UserProjection) -> Unit) {
    repository.findById(identifier).get().also {
      stateChanges.invoke(it)
      saveProjection(it)
    }
  }

  private fun saveProjection(projection: UserProjection) {
    repository.save(projection).also { savedProjection -> updateQuerySubscribers(savedProjection) }
  }

  private fun updateQuerySubscribers(user: UserProjection) {
    queryUpdateEmitter.emit<FindUserByExternalUserIdQuery, UserQueryResult>(user.toQueryResult()) {
        query ->
      query.externalUserId == user.externalUserId
    }
    queryUpdateEmitter.emit<UserQuery, UserQueryResult>(user.toQueryResult()) { query ->
      query.userId == user.identifier
    }
    queryUpdateEmitter.emit<AllUsersQuery, UserQueryResult>(user.toQueryResult()) { true }
  }

  @ResetHandler fun reset() = repository.deleteAll()

  @QueryHandler
  fun handle(query: FindUserByExternalUserIdQuery): Optional<UserQueryResult> =
      repository.findByExternalUserId(query.externalUserId).map { it.toQueryResult() }

  @QueryHandler
  fun handle(query: UserQuery): Optional<UserQueryResult> =
      repository.findById(query.userId).map { it.toQueryResult() }

  @QueryHandler
  fun handle(query: AllUsersQuery): Iterable<UserQueryResult> =
      repository.findAll().map { it.toQueryResult() }
}
