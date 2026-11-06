package hr.algebra.project_manager.task.api;

import hr.algebra.project_manager.common.WorkStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Request for updating the status of a task")
public record TaskStatusRequest(

        @Schema(description = "New status of the task", example = "IN_PROGRESS")
        @NotNull WorkStatus status
        ) {
}
