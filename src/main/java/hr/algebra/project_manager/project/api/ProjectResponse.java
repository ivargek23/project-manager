package hr.algebra.project_manager.project.api;

import hr.algebra.project_manager.common.WorkStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "Response for a project")
public record ProjectResponse (

        @Schema(description = "Id of the project", example = "1")
        Long id,

        @Schema(description = "Name of the project", example = "Website redesign")
        String name,

        @Schema(description = "Description of the project", example = "Redesign the website to improve user experience")
        String description,

        @Schema(description = "Start date of the project", example = "2026-01-01")
        LocalDate startDate,
        @Schema(description = "Due date of the project", example = "2026-01-31")
        LocalDate dueDate,
        @Schema(description = "Status of the project", example = "IN_PROGRESS")
        WorkStatus status,
        @Schema(description = "Name of the project lead", example = "John Doe")
        String leadName,
        @Schema(description = "Version of the project", example = "1")
        Long version
){
}
