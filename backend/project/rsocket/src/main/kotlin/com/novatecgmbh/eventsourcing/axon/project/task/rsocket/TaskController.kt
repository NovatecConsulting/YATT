package com.novatecgmbh.eventsourcing.axon.project.task.rsocket

import com.novatecgmbh.eventsourcing.axon.project.project.api.ProjectId
import com.novatecgmbh.eventsourcing.axon.project.task.api.TaskId
import com.novatecgmbh.eventsourcing.axon.project.task.api.TaskQuery
import com.novatecgmbh.eventsourcing.axon.project.task.api.TaskQueryResult
import com.novatecgmbh.eventsourcing.axon.project.task.api.TasksByProjectQuery
import org.axonframework.extensions.reactor.queryhandling.gateway.ReactorQueryGateway
import org.springframework.messaging.handler.annotation.DestinationVariable
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.stereotype.Controller
import reactor.core.publisher.Flux

@Controller
class TaskController(val queryGateway: ReactorQueryGateway) {

  @MessageMapping("tasks.{id}")
  fun subscribeTaskByIdUpdates(@DestinationVariable id: TaskId): Flux<TaskQueryResult> =
      queryGateway.queryUpdates(TaskQuery(id), TaskQueryResult::class.java)

  @MessageMapping("projects.{id}.tasks")
  fun subscribeTaskByProjectUpdates(@DestinationVariable id: ProjectId): Flux<TaskQueryResult> =
      queryGateway.queryUpdates(TasksByProjectQuery(id), TaskQueryResult::class.java)
}
