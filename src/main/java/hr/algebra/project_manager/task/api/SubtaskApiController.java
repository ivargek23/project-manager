package hr.algebra.project_manager.task.api;

import hr.algebra.project_manager.task.service.SubtaskRow;
import hr.algebra.project_manager.task.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/projects/{projectId}/tasks/{taskId}/subtasks")
@RequiredArgsConstructor
@Tag(name = "Subtasks", description = "API endpoints for managing subtasks")
public class SubtaskApiController {

    private final TaskService taskService;
    private static final String PROBLEM = MediaType.APPLICATION_PROBLEM_JSON_VALUE;

    @GetMapping
    @Operation(summary = "Get subtasks", description = "Retrieves a list of subtasks for a specific task in a project")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Subtasks of the task"),
            @ApiResponse(responseCode = "403", description = "Not a member of the project",
                    content = @Content(mediaType = PROBLEM, schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Task not found in this project",
                    content = @Content(mediaType = PROBLEM, schema = @Schema(implementation = ProblemDetail.class)))
    })
    public List<SubtaskRow> getSubtasks(@PathVariable Long projectId, @PathVariable Long taskId) {
        return taskService.findSubtasks(projectId, taskId);
    }

    @GetMapping("/{subtaskId}")
    @Operation(summary = "Get a subtask", description = "Retrieves a specific subtask for a task in a project")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "The subtask"),
            @ApiResponse(responseCode = "403", description = "Not a member of the project",
                    content = @Content(mediaType = PROBLEM, schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Task or subtask not found",
                    content = @Content(mediaType = PROBLEM, schema = @Schema(implementation = ProblemDetail.class)))
    })
    public SubtaskRow getSubtask(@PathVariable Long projectId, @PathVariable Long taskId,
                                 @PathVariable Long subtaskId) {
        return taskService.getSubtask(projectId, taskId, subtaskId);
    }

    @PostMapping
    @Operation(summary = "Add a subtask", description = "Adds a new subtask to a task in a project")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Subtask created; Location header points to it"),
            @ApiResponse(responseCode = "400", description = "Title missing or too long",
                    content = @Content(mediaType = PROBLEM, schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "403", description = "Only the project lead or the task assignee",
                    content = @Content(mediaType = PROBLEM, schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Task not found in this project",
                    content = @Content(mediaType = PROBLEM, schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<SubtaskRow> addSubtask(@PathVariable Long projectId, @PathVariable Long taskId,
                                                 @Valid @RequestBody SubtaskRequest request) {
        Long subtaskId = taskService.addSubtask(projectId, taskId, request.title());
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{subtaskId}")
                .buildAndExpand(subtaskId)
                .toUri();
        return ResponseEntity.created(location).body(taskService.getSubtask(projectId, taskId, subtaskId));
    }

    @PatchMapping("/{subtaskId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Set subtask done", description = "Updates the done status of a subtask for a task in a project")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Done status updated"),
            @ApiResponse(responseCode = "400", description = "The done field is missing",
                    content = @Content(mediaType = PROBLEM, schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "403", description = "Only the project lead or the task assignee",
                    content = @Content(mediaType = PROBLEM, schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Task or subtask not found",
                    content = @Content(mediaType = PROBLEM, schema = @Schema(implementation = ProblemDetail.class)))
    })
    public void setDone(@PathVariable Long projectId, @PathVariable Long taskId,
                        @PathVariable Long subtaskId, @Valid @RequestBody SubtaskDoneRequest request) {
        taskService.setSubtaskDone(projectId, taskId, subtaskId, request.done());
    }

    @DeleteMapping("/{subtaskId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete a subtask", description = "Deletes a specific subtask for a task in a project")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Subtask deleted"),
            @ApiResponse(responseCode = "403", description = "Only the project lead or the task assignee",
                    content = @Content(mediaType = PROBLEM, schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Task or subtask not found",
                    content = @Content(mediaType = PROBLEM, schema = @Schema(implementation = ProblemDetail.class)))
    })
    public void deleteSubtask(@PathVariable Long projectId, @PathVariable Long taskId,
                              @PathVariable Long subtaskId) {
        taskService.deleteSubtask(projectId, taskId, subtaskId);
    }
}
