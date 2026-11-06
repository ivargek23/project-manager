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

        @Schema(description = "Start date of the project", example = "2023-01-01")
        @NotNull LocalDate startDate,
        @Schema(description = "Due date of the project", example = "2023-01-31")
        @NotNull LocalDate dueDate,
        @Schema(description = "Status of the project", example = "IN_PROGRESS")
        @NotNull WorkStatus status,
        @Schema(description = "Id of the project lead", example = "1")
        Long leadId,
        @Schema(description = "Version of the project", example = "1")
        Long version
        ) {
}
