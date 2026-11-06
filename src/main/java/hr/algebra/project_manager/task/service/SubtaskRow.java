package hr.algebra.project_manager.task.service;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Subtask row")
public record SubtaskRow(

        @Schema(description = "Id of the subtask", example = "1")
        Long id,

        @Schema(description = "Title of the subtask", example = "Subtask title")
        String title,

        @Schema(description = "Done status of the subtask", example = "false")
        boolean done
) {
}
