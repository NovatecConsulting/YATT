package com.novatecgmbh.eventsourcing.axon.user.user.graphql

import com.novatecgmbh.eventsourcing.axon.application.security.UnregisteredUserPrincipal
import com.novatecgmbh.eventsourcing.axon.user.api.*
import java.util.concurrent.CompletableFuture
import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.extensions.kotlin.queryMany
import org.axonframework.queryhandling.QueryGateway
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.MutationMapping
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Controller

@Controller
class UserController(val commandGateway: CommandGateway, val queryGateway: QueryGateway) {

  @QueryMapping
  fun users(): CompletableFuture<List<UserQueryResult>> = queryGateway.queryMany(AllUsersQuery())

  @MutationMapping
  fun registerUser(
      @Argument firstname: String,
      @Argument lastname: String,
      @Argument email: String,
      @Argument telephone: String,
  ): CompletableFuture<UserId> =
      SecurityContextHolder.getContext().authentication.principal.let {
        if (it is UnregisteredUserPrincipal) {
          commandGateway.send(
              RegisterUserCommand(UserId(), it.username, firstname, lastname, email, telephone))
        } else {
          throw IllegalStateException("User already registered")
        }
      }

  @MutationMapping
  fun renameUser(
      @Argument identifier: UserId,
      @Argument firstname: String,
      @Argument lastname: String
  ): CompletableFuture<Long> =
      commandGateway.send(RenameUserCommand(identifier, firstname, lastname))
}
