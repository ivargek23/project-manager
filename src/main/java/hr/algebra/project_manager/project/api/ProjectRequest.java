package hr.algebra.project_manager.project.api;

import hr.algebra.project_manager.common.WorkStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

@Schema(description = "Request for creating or updating a project")
public record ProjectRequest(
        @NotBlank
        @Size(max = 150)
        @Schema(description = "Name of the project", example = "Website redesign")
        String name,

        @Schema(description = "Description of the project", example = "Redesign the website to improve user experience")
        String description,

        @Schema(description = "Start date of the project", example = "2026-01-01")
        @NotNull LocalDate startDate,
        @Schema(description = "Due date of the project", example = "2026-01-31")
        @NotNull LocalDate dueDate,
        @Schema(description = "Status of the project", example = "IN_PROGRESS")
        @NotNull WorkStatus status,
        @Schema(description = "Lead's user id. Only used when an admin creates or updates; ignored for project leads, who always lead their own projects.", example = "2")
        Long leadId,

        @Schema(description = "Version from the last read. Required on update for the stale-version check; omit on create.", example = "0")
        Long version
        ) {
}
