package hr.algebra.project_manager.dependency.api;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Request for creating or updating a dependency")
public record DependencyRequest(
        @Schema(description = "Task that must finish first", example = "101")
        @NotNull Long predecessorId,

        @Schema(description = "Task that waits for the predecessor", example = "102")
        @NotNull Long successorId,

        @Schema(description = "Working days to wait after the predecessor finishes", example = "0")
        @NotNull @Min(0) @Max(100) Integer lagDays
) {
}