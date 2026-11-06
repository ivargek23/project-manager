package hr.algebra.project_manager.task.api;

import hr.algebra.project_manager.common.WorkStatus;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

public record TaskRequest(
        @NotBlank @Size(max = 200) String title,
        @Size(max = 2000) String description,
        Long assigneeId,
        @NotNull WorkStatus status,
        @NotNull @Min(1) @Max(100) Integer estimateDays,
        LocalDate dueDate,
        Long version
) {
}
