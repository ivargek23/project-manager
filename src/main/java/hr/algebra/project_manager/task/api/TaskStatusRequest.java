package hr.algebra.project_manager.task.api;

import hr.algebra.project_manager.common.WorkStatus;
import jakarta.validation.constraints.NotNull;

public record TaskStatusRequest(
        @NotNull WorkStatus status
        ) {
}
