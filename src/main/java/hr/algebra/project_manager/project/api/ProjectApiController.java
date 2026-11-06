package hr.algebra.project_manager.project.api;

import hr.algebra.project_manager.common.WorkStatus;
import hr.algebra.project_manager.project.mapper.ProjectMapper;
import hr.algebra.project_manager.project.repository.ProjectSummary;
import hr.algebra.project_manager.project.service.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectApiController {
    private final ProjectService projectService;
    private final ProjectMapper projectMapper;

    @GetMapping
    public PagedModel<ProjectSummary> getProjects(
            @RequestParam(defaultValue = "") String q,
            @RequestParam(required = false) WorkStatus status,
            @PageableDefault(size = 10, sort = "name") Pageable pageable) {
        return new PagedModel<>(projectService.search(q, status, pageable));
    }

    @GetMapping("/{id}")
    public ProjectResponse getProjectById(
            @PathVariable Long id) {
        return projectMapper.toProjectResponse(projectService.get(id));
    }

    @PostMapping
    public ResponseEntity<ProjectResponse> createProject(@Valid @RequestBody ProjectRequest request) {
        Long id = projectService.create(projectMapper.projectRequestToCommand(request));

        URI path = buildProjectUri(id);

        return ResponseEntity.created(path).body(projectMapper.toProjectResponse(projectService.get(id)));
    }

    @PutMapping("/{id}")
    public ProjectResponse updateProject(@PathVariable Long id,
                                         @Valid @RequestBody ProjectRequest request) {
        projectService.update(id, request.version(), projectMapper.projectRequestToCommand(request));
        return projectMapper.toProjectResponse(projectService.get(id));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProject(@PathVariable Long id) {
        projectService.delete(id);
    }

    private static @NonNull URI buildProjectUri(Long id) {
        return ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(id)
                .toUri();
    }
}
