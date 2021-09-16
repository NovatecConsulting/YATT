import com.novatecgmbh.eventsourcing.axon.project.project.command.ProjectId
import com.novatecgmbh.eventsourcing.axon.project.task.command.TaskId
import java.time.LocalDate
import org.axonframework.modelling.command.TargetAggregateIdentifier

abstract class TaskCommand(
    open val identifier: TaskId,
)

data class CreateTaskCommand(
    @TargetAggregateIdentifier override val identifier: TaskId,
    val projectId: ProjectId,
    val name: String,
    val description: String?,
    val startDate: LocalDate,
    val endDate: LocalDate
) : TaskCommand(identifier)

data class ChangeTaskDescriptionCommand(
    @TargetAggregateIdentifier override val identifier: TaskId,
    val name: String,
    val description: String?
) : TaskCommand(identifier)

data class RescheduleTaskCommand(
    @TargetAggregateIdentifier override val identifier: TaskId,
    val startDate: LocalDate,
    val endDate: LocalDate
) : TaskCommand(identifier)

data class StartTaskCommand(@TargetAggregateIdentifier override val identifier: TaskId) :
    TaskCommand(identifier)

data class CompleteTaskCommand(@TargetAggregateIdentifier override val identifier: TaskId) :
    TaskCommand(identifier)
