import com.novatecgmbh.eventsourcing.axon.project.project.command.ProjectId
import com.novatecgmbh.eventsourcing.axon.project.task.command.TaskId
import java.time.LocalDate

abstract class TaskEvent(open val identifier: TaskId)

data class TaskCreatedEvent(
    override val identifier: TaskId,
    val projectId: ProjectId,
    val name: String,
    val description: String?,
    val startDate: LocalDate,
    val endDate: LocalDate
) : TaskEvent(identifier)

data class TaskDescriptionUpdatedEvent(
    override val identifier: TaskId,
    val name: String,
    val description: String?
) : TaskEvent(identifier)

data class TaskRescheduledEvent(
    override val identifier: TaskId,
    val startDate: LocalDate,
    val endDate: LocalDate
) : TaskEvent(identifier)

data class TaskStartedEvent(override val identifier: TaskId) : TaskEvent(identifier)

data class TaskCompletedEvent(override val identifier: TaskId) : TaskEvent(identifier)
