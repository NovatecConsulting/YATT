package com.novatecgmbh.eventsourcing.axon.user.web

import com.novatecgmbh.eventsourcing.axon.application.security.RegisteredUserPrincipal
import com.novatecgmbh.eventsourcing.axon.application.security.UnregisteredUserPrincipal
import com.novatecgmbh.eventsourcing.axon.user.api.*
import com.novatecgmbh.eventsourcing.axon.user.web.dto.RegisterUserDto
import com.novatecgmbh.eventsourcing.axon.user.web.dto.RenameUserDto
import java.time.Duration
import java.util.concurrent.CompletableFuture
import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.extensions.kotlin.queryMany
import org.axonframework.extensions.kotlin.queryOptional
import org.axonframework.messaging.responsetypes.ResponseTypes
import org.axonframework.queryhandling.QueryGateway
import org.axonframework.queryhandling.SubscriptionQueryResult
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono

@RequestMapping("/v2/users")
@RestController
class UserController(
    private val commandGateway: CommandGateway,
    private val queryGateway: QueryGateway,
) {

  @GetMapping
  fun getAllUsers(): CompletableFuture<List<UserQueryResult>> =
      queryGateway.queryMany(AllUsersQuery())

  @GetMapping("/current")
  fun getCurrentUser(
      @AuthenticationPrincipal principal: UserDetails
  ): ResponseEntity<UserQueryResult> =
      queryGateway
          .queryOptional<UserQueryResult, FindUserByExternalUserIdQuery>(
              FindUserByExternalUserIdQuery(principal.username))
          .get()
          .map { ResponseEntity(it, HttpStatus.OK) }
          .orElse(ResponseEntity(HttpStatus.NOT_FOUND))

  @PostMapping("/current")
  fun registerUser(
      @RequestBody body: RegisterUserDto,
      @AuthenticationPrincipal principal: UnregisteredUserPrincipal
  ): Mono<ResponseEntity<UserQueryResult>> =
      queryGateway.subscriptionQuery(
              FindUserByExternalUserIdQuery(principal.username),
              ResponseTypes.instanceOf(UserQueryResult::class.java),
              ResponseTypes.instanceOf(UserQueryResult::class.java),
          )
          .let { queryResult -> wrapCommand(queryResult, body.toCommand(principal.username)) }

  @PostMapping("/current/rename")
  fun renameUser(
      @RequestBody body: RenameUserDto,
      @AuthenticationPrincipal principal: RegisteredUserPrincipal
  ): Mono<ResponseEntity<UserQueryResult>> =
      queryGateway.subscriptionQuery(
              UserQuery(principal.identifier),
              ResponseTypes.instanceOf(UserQueryResult::class.java),
              ResponseTypes.instanceOf(UserQueryResult::class.java),
          )
          .let { queryResult -> wrapCommand(queryResult, body.toCommand(principal.identifier)) }

  fun <T, R> wrapCommand(queryResult: SubscriptionQueryResult<T, T>, command: R) =
      Mono.`when`(queryResult.initialResult())
          .then(Mono.fromCompletionStage { commandGateway.send<Unit>(command) })
          .thenMany(queryResult.updates())
          .next()
          .map { entity -> ResponseEntity.ok(entity) }
          .timeout(Duration.ofSeconds(5))
          .doFinally { queryResult.cancel() }
}
