package com.novatecgmbh.eventsourcing.axon.user.user.web

import com.novatecgmbh.eventsourcing.axon.application.security.RegisteredUserPrincipal
import com.novatecgmbh.eventsourcing.axon.application.security.UnregisteredUserPrincipal
import com.novatecgmbh.eventsourcing.axon.user.api.AllUsersQuery
import com.novatecgmbh.eventsourcing.axon.user.api.FindUserByExternalUserIdQuery
import com.novatecgmbh.eventsourcing.axon.user.api.UserId
import com.novatecgmbh.eventsourcing.axon.user.api.UserQueryResult
import com.novatecgmbh.eventsourcing.axon.user.user.web.dto.RegisterUserDto
import com.novatecgmbh.eventsourcing.axon.user.user.web.dto.RenameUserDto
import java.util.concurrent.CompletableFuture
import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.extensions.kotlin.queryMany
import org.axonframework.extensions.kotlin.queryOptional
import org.axonframework.queryhandling.QueryGateway
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*

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
  ): CompletableFuture<UserId> = commandGateway.send(body.toCommand(principal.username))

  @PostMapping("/current/rename")
  fun renameUser(
      @RequestBody body: RenameUserDto,
      @AuthenticationPrincipal principal: RegisteredUserPrincipal
  ): CompletableFuture<UserId> = commandGateway.send(body.toCommand(principal.identifier))
}
