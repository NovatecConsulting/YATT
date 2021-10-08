package com.novatecgmbh.eventsourcing.axon.user.web

import com.novatecgmbh.eventsourcing.axon.security.UnregisteredUserPrincipal
import com.novatecgmbh.eventsourcing.axon.user.api.FindUserByExternalUserIdQuery
import com.novatecgmbh.eventsourcing.axon.user.api.UserId
import com.novatecgmbh.eventsourcing.axon.user.api.UserQueryResult
import com.novatecgmbh.eventsourcing.axon.user.web.dto.RegisterUserDto
import java.util.concurrent.CompletableFuture
import org.axonframework.commandhandling.gateway.CommandGateway
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
}
