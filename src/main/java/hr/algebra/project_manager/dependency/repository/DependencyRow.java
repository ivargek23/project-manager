package hr.algebra.project_manager.dependency.repository;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "A dependency between the two tasks of the same project")
public record DependencyRow(
        @Schema(description = "Dependency id", example = "7") Long id,
        @Schema(description = "Task that must finish first", example = "101") Long predecessorId,
        @Schema(example = "Research & content audit") String predecessorTitle,
        @Schema(description = "Task that waits for the predecessor", example = "102") Long successorId,
        @Schema(example = "Information architecture") String successorTitle,
        @Schema(description = "Working days to wait after the predecessor finishes", example = "0") int lagDays
) {
}
