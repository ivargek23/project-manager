package hr.algebra.project_manager.task.api;

import hr.algebra.project_manager.common.WorkStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

@Schema(description = "Request for creating or updating a task")
public record TaskRequest(
        @NotBlank @Size(max = 200)
        @Schema(description = "Title of the task", example = "Fix bug in login page")
        String title,

        @Schema(description = "Description of the task", example = "Fix bug in login page")
        @Size(max = 2000) String description,
        Long assigneeId,

       @Schema(description = "Status of the task", example = "IN_PROGRESS")
        @NotNull WorkStatus status,

        @Schema(description = "Estimate days for the task", example = "5")
        @NotNull @Min(1) @Max(100) Integer estimateDays,

        @Schema(description = "Due date of the task", example = "2023-01-01")
        LocalDate dueDate,

        @Schema(description = "Version from the last read. Required on update for the stale-version check; omit on create.", example = "0")
        Long version
) {
}
