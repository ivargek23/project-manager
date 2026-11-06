package hr.algebra.project_manager.project.api;

import hr.algebra.project_manager.common.WorkStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record ProjectRequest(
        @NotBlank
        @Size(max = 150)
        String name,
        String description,
        @NotNull LocalDate startDate,
        @NotNull LocalDate dueDate,
        @NotNull WorkStatus status,
        Long leadId,
        Long version
        ) {
}
