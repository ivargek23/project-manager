package hr.algebra.project_manager.task.api;

import jakarta.validation.constraints.NotNull;

public record SubtaskDoneRequest(@NotNull Boolean done) {
}
