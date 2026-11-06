package hr.algebra.project_manager.task.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SubtaskRequest(@NotBlank @Size(max = 200) String title) {
}
