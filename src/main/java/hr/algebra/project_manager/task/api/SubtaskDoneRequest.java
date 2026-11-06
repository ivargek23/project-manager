package hr.algebra.project_manager.task.api;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Request for marking a subtask as done")
public record SubtaskDoneRequest(
        @NotNull @Schema(description = "Whether the subtask is done", example = "true")
        Boolean done) {
}
