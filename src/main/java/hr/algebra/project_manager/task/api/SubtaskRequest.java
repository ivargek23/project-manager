package hr.algebra.project_manager.task.api;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Request for creating or updating a subtask")
public record SubtaskRequest(
        @Schema(description = "Title of the subtask", example = "Fix bug in login page")
        @NotBlank @Size(max = 200) String title) {
}
