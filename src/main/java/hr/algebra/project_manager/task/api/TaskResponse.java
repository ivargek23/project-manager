package hr.algebra.project_manager.task.api;

import hr.algebra.project_manager.common.WorkStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "Response for a task")
public record TaskResponse(

        @Schema(description = "ID of the task", example = "123")
        Long id,

        @Schema(description = "Title of the task", example = "Implement new feature")
        String title,

        @Schema(description = "Description of the task", example = "Add new feature to the login page")
        String description,

        @Schema(description = "Status of the task", example = "IN_PROGRESS")
        WorkStatus status,

        @Schema(description = "Estimated number of days to complete the task", example = "5")
        Integer estimateDays,

        @Schema(description = "Due date of the task", example = "2023-05-15")
        LocalDate dueDate,

        @Schema(description = "ID of the assignee", example = "456")
        Long assigneeId,

        @Schema(description = "Name of the assignee", example = "John Doe")
        String assigneeName,

        @Schema(description = "Version of the task", example = "1")
        Long version
) {
}
