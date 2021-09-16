package com.novatecgmbh.eventsourcing.axon.admin.web

import com.novatecgmbh.eventsourcing.axon.admin.AxonAdministration
import java.util.*
import org.axonframework.eventhandling.EventProcessor
import org.axonframework.eventhandling.EventTrackerStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/admin/eventprocessors")
class AdminController(private val axonAdministration: AxonAdministration) {

  @GetMapping
  fun getEventProcessors(): List<EventProcessor> = axonAdministration.getTrackingEventProcessors()

  @GetMapping("/{groupName}")
  fun getEventProcessor(@PathVariable groupName: String): Optional<EventProcessor> =
      axonAdministration.getEventProcessor(groupName)

  /*
  Returns a map where the key is the segment identifier, and the value is the event processing status.
  Based on this status we can determine whether the Processor is caught up and/or is replaying.
  This can be used to implement Blue-Green deployment. You don't want to send queries to 'view model' if Processor is not caught up and/or is replaying.
  */
  @GetMapping("/{groupName}/status")
  fun getEventProcessorStatus(@PathVariable groupName: String): Map<Int, EventTrackerStatus> =
      axonAdministration.getTrackingEventProcessorStatus(groupName)

  @PostMapping("/{groupName}/reset")
  fun resetEventProcessor(@PathVariable groupName: String): ResponseEntity<Any> {
    axonAdministration.resetTrackingEventProcessor(groupName)
    return ResponseEntity.accepted().build()
  }
}
