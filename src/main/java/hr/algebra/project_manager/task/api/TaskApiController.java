package hr.algebra.project_manager.task.api;

import hr.algebra.project_manager.common.validation.SortWhitelist;
import hr.algebra.project_manager.task.mapper.TaskMapper;
import hr.algebra.project_manager.task.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.Set;

@RestController
@RequestMapping("/api/projects/{projectId}/tasks")
@RequiredArgsConstructor
@Tag(name = "Tasks", description = "API endpoints for managing tasks")
public class TaskApiController {

    private static final Set<String> SORTABLE = Set.of("title", "dueDate", "status", "estimateDays");
    private static final String PROBLEM = MediaType.APPLICATION_PROBLEM_JSON_VALUE;

    private final TaskService taskService;
    private final TaskMapper taskMapper;

    @GetMapping
    @Operation(summary = "List tasks of a project",
            description = "Paged list of the project's tasks. Sortable by title, dueDate, status, estimateDays.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "One page of tasks"),
            @ApiResponse(responseCode = "400", description = "Unknown sort field",
                    content = @Content(mediaType = PROBLEM, schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "403", description = "Not the lead or a member of this project",
                    content = @Content(mediaType = PROBLEM, schema = @Schema(implementation = ProblemDetail.class)))
    })
    public PagedModel<TaskResponse> getTasks(
            @PathVariable Long projectId,
            @ParameterObject @PageableDefault(size = 10, sort = "name") Pageable pageable) {
        return new PagedModel<>(taskService.findPage(projectId, SortWhitelist.restrict(pageable, SORTABLE))
                .map(taskMapper::toTaskResponse));
    }

    @GetMapping("/{taskId}")
    @Operation(summary = "Get a task", description = "Retrieves a specific task of the project")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "The task"),
            @ApiResponse(responseCode = "403", description = "Not the lead or a member of this project",
                    content = @Content(mediaType = PROBLEM, schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Task not found in this project",
                    content = @Content(mediaType = PROBLEM, schema = @Schema(implementation = ProblemDetail.class)))
    })
    public TaskResponse getTask(@PathVariable Long projectId, @PathVariable Long taskId) {
        return taskMapper.toTaskResponse(taskService.get(projectId, taskId));
    }

    @PostMapping
    @Operation(summary = "Create a task", description = "Creates a new task in the project. Only the project lead.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Task created; Location header points to it"),
            @ApiResponse(responseCode = "400", description = "Validation failed (see errors)",
                    content = @Content(mediaType = PROBLEM, schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "403", description = "Only the lead of this project",
                    content = @Content(mediaType = PROBLEM, schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "422", description = "The assignee is not a member of this project",
                    content = @Content(mediaType = PROBLEM, schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<TaskResponse> createTask(@PathVariable Long projectId,
                                                   @Valid @RequestBody TaskRequest request) {
        Long taskId = taskService.createTask(projectId, taskMapper.taskRequestToCommand(request));
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{taskId}")
                .buildAndExpand(taskId)
                .toUri();
        return ResponseEntity.created(location)
                .body(taskMapper.toTaskResponse(taskService.get(projectId, taskId)));
    }

    @PutMapping("/{taskId}")
    @Operation(summary = "Update a task",
            description = "Full update of a task. Only the project lead; send the version from the last read. "
                    + "Assignees change the status with PATCH /status.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "The updated task"),
            @ApiResponse(responseCode = "400", description = "Validation failed (see errors)",
                    content = @Content(mediaType = PROBLEM, schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "403", description = "Only the lead of this project",
                    content = @Content(mediaType = PROBLEM, schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Task not found in this project",
                    content = @Content(mediaType = PROBLEM, schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "409", description = "Stale version: the task was changed by someone else",
                    content = @Content(mediaType = PROBLEM, schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "422", description = "The assignee is not a member of this project",
                    content = @Content(mediaType = PROBLEM, schema = @Schema(implementation = ProblemDetail.class)))
    })
    public TaskResponse updateTask(@PathVariable Long projectId,
                                   @PathVariable Long taskId,
                                   @Valid @RequestBody TaskRequest request) {
        taskService.updateTask(projectId, taskId, request.version(), taskMapper.taskRequestToCommand(request));
        return taskMapper.toTaskResponse(taskService.get(projectId, taskId));
    }

    @PatchMapping("/{taskId}/status")
    @Operation(summary = "Update task status",
            description = "Changes only the status. Allowed for the project lead and the task's assignee.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "The task with its new status"),
            @ApiResponse(responseCode = "400", description = "Status missing or not a valid value",
                    content = @Content(mediaType = PROBLEM, schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "403", description = "Only the project lead or the task's assignee",
                    content = @Content(mediaType = PROBLEM, schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Task not found in this project",
                    content = @Content(mediaType = PROBLEM, schema = @Schema(implementation = ProblemDetail.class)))
    })
    public TaskResponse updateStatus(@PathVariable Long projectId,
                                     @PathVariable Long taskId,
                                     @Valid @RequestBody TaskStatusRequest request) {
        taskService.updateStatus(projectId, taskId, request.status());
        return taskMapper.toTaskResponse(taskService.get(projectId, taskId));
    }

    @DeleteMapping("/{taskId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete a task", description = "Deletes a task of the project. Only the project lead.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Task deleted"),
            @ApiResponse(responseCode = "403", description = "Only the lead of this project",
                    content = @Content(mediaType = PROBLEM, schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Task not found in this project",
                    content = @Content(mediaType = PROBLEM, schema = @Schema(implementation = ProblemDetail.class)))
    })
    public void deleteTask(@PathVariable Long projectId, @PathVariable Long taskId) {
        taskService.deleteTask(projectId, taskId);
    }
}