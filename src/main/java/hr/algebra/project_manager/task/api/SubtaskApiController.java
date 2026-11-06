package hr.algebra.project_manager.task.api;

import hr.algebra.project_manager.task.service.SubtaskRow;
import hr.algebra.project_manager.task.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/projects/{projectId}/tasks/{taskId}/subtasks")
@RequiredArgsConstructor
public class SubtaskApiController {

    private final TaskService taskService;

    @GetMapping
    public List<SubtaskRow> getSubtasks(@PathVariable Long projectId, @PathVariable Long taskId) {
        return taskService.findSubtasks(projectId, taskId);
    }

    @GetMapping("/{subtaskId}")
    public SubtaskRow getSubtask(@PathVariable Long projectId, @PathVariable Long taskId,
                                 @PathVariable Long subtaskId) {
        return taskService.getSubtask(projectId, taskId, subtaskId);
    }

    @PostMapping
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
    public void setDone(@PathVariable Long projectId, @PathVariable Long taskId,
                        @PathVariable Long subtaskId, @Valid @RequestBody SubtaskDoneRequest request) {
        taskService.setSubtaskDone(projectId, taskId, subtaskId, request.done());
    }

    @DeleteMapping("/{subtaskId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteSubtask(@PathVariable Long projectId, @PathVariable Long taskId,
                              @PathVariable Long subtaskId) {
        taskService.deleteSubtask(projectId, taskId, subtaskId);
    }
}
