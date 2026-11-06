package hr.algebra.project_manager.task.api;

import hr.algebra.project_manager.common.validation.SortWhitelist;
import hr.algebra.project_manager.task.mapper.TaskMapper;
import hr.algebra.project_manager.task.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.Set;

@RestController
@RequestMapping("/api/projects/{projectId}/tasks")
@RequiredArgsConstructor
public class TaskApiController {

    private final TaskService taskService;
    private final TaskMapper taskMapper;
    private static final Set<String> SORTABLE = Set.of("title", "dueDate", "status", "estimateDays");

    @GetMapping
    public PagedModel<TaskResponse> getTasks(
            @PathVariable Long projectId,
            @PageableDefault(size = 20, sort = "dueDate") Pageable pageable
    ) {
        return new PagedModel<>(taskService.findPage(projectId, SortWhitelist.restrict(pageable, SORTABLE))
                .map(taskMapper::toTaskResponse));
    }

    @GetMapping("/{taskId}")
    public TaskResponse getTask(@PathVariable Long projectId, @PathVariable Long taskId) {
        return taskMapper.toTaskResponse(taskService.get(projectId, taskId));
    }

    @PostMapping
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
    public TaskResponse updateTask(@PathVariable Long projectId,
                                   @PathVariable Long taskId,
                                   @Valid @RequestBody TaskRequest request) {
        taskService.updateTask(projectId, taskId, request.version(), taskMapper.taskRequestToCommand(request));
        return taskMapper.toTaskResponse(taskService.get(projectId, taskId));
    }

    @PatchMapping("/{taskId}/status")
    public TaskResponse updateStatus(@PathVariable Long projectId,
                                     @PathVariable Long taskId,
                                     @Valid @RequestBody TaskStatusRequest request) {
        taskService.updateStatus(projectId, taskId, request.status());
        return taskMapper.toTaskResponse(taskService.get(projectId, taskId));
    }

    @DeleteMapping("/{taskId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTask(@PathVariable Long projectId, @PathVariable Long taskId) {
        taskService.deleteTask(projectId, taskId);
    }
}
