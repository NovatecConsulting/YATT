package com.novatecgmbh.eventsourcing.axon.user.user.graphql

import com.novatecgmbh.eventsourcing.axon.application.security.UnregisteredUserPrincipal
import com.novatecgmbh.eventsourcing.axon.user.api.RegisterUserCommand
import com.novatecgmbh.eventsourcing.axon.user.api.RenameUserCommand
import com.novatecgmbh.eventsourcing.axon.user.api.UserId
import java.util.concurrent.CompletableFuture
import org.axonframework.commandhandling.gateway.CommandGateway
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.MutationMapping
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Controller

@Controller
class UserMutationsController(val commandGateway: CommandGateway) {

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
