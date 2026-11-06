package hr.algebra.project_manager.project.api;

import hr.algebra.project_manager.common.WorkStatus;
import hr.algebra.project_manager.common.validation.SortWhitelist;
import hr.algebra.project_manager.project.mapper.ProjectMapper;
import hr.algebra.project_manager.project.repository.ProjectSummary;
import hr.algebra.project_manager.project.service.ProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
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
@RequestMapping("/api/projects")
@RequiredArgsConstructor
@Tag(name = "Projects", description = "REST API for managing projects")
public class ProjectApiController {

    private static final Set<String> SORTABLE = Set.of("name", "dueDate", "status");
    private static final String PROBLEM = MediaType.APPLICATION_PROBLEM_JSON_VALUE;

    private final ProjectService projectService;
    private final ProjectMapper projectMapper;

    @GetMapping
    @Operation(summary = "List projects",
            description = "Paged list of projects visible to the current user: all projects for an admin, "
                    + "otherwise the projects the user leads or is a member of. Sortable by name, dueDate, status.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "One page of projects"),
            @ApiResponse(responseCode = "400", description = "Unknown sort field",
                    content = @Content(mediaType = PROBLEM, schema = @Schema(implementation = ProblemDetail.class)))
    })
    public PagedModel<ProjectSummary> getProjects(
            @RequestParam(defaultValue = "") String q,
            @RequestParam(required = false) WorkStatus status,
            @ParameterObject @PageableDefault(size = 10, sort = "name") Pageable pageable) {
        return new PagedModel<>(projectService.search(q, status, SortWhitelist.restrict(pageable, SORTABLE)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a project by ID", description = "Returns a project with the specified ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "The project"),
            @ApiResponse(responseCode = "403", description = "Not the lead or a member of this project",
                    content = @Content(mediaType = PROBLEM, schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Project not found",
                    content = @Content(mediaType = PROBLEM, schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ProjectResponse getProjectById(@PathVariable Long id) {
        return projectMapper.toProjectResponse(projectService.get(id));
    }

    @PostMapping
    @Operation(summary = "Create a new project",
            description = "Creates a project. A project lead always becomes the lead of the new project; "
                    + "only an admin can choose the lead with leadId.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Project created; Location header points to it"),
            @ApiResponse(responseCode = "400", description = "Validation failed (see errors)",
                    content = @Content(mediaType = PROBLEM, schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "403", description = "Only admins and project leads can create projects",
                    content = @Content(mediaType = PROBLEM, schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "422", description = "The chosen lead is disabled or cannot lead projects",
                    content = @Content(mediaType = PROBLEM, schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<ProjectResponse> createProject(@Valid @RequestBody ProjectRequest request) {
        Long id = projectService.create(projectMapper.projectRequestToCommand(request));
        return ResponseEntity.created(buildProjectUri(id))
                .body(projectMapper.toProjectResponse(projectService.get(id)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a project",
            description = "Updates a project. Send the version from the last read; only an admin can change the lead.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "The updated project"),
            @ApiResponse(responseCode = "400", description = "Validation failed (see errors)",
                    content = @Content(mediaType = PROBLEM, schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "403", description = "Only an admin or the lead of this project",
                    content = @Content(mediaType = PROBLEM, schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Project not found",
                    content = @Content(mediaType = PROBLEM, schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "409", description = "Stale version: the project was changed by someone else",
                    content = @Content(mediaType = PROBLEM, schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "422", description = "The chosen lead is disabled or cannot lead projects",
                    content = @Content(mediaType = PROBLEM, schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ProjectResponse updateProject(@PathVariable Long id,
                                         @Valid @RequestBody ProjectRequest request) {
        projectService.update(id, request.version(), projectMapper.projectRequestToCommand(request));
        return projectMapper.toProjectResponse(projectService.get(id));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete a project", description = "Deletes an existing project with the specified ID")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Project deleted"),
            @ApiResponse(responseCode = "403", description = "Only an admin or the lead of this project",
                    content = @Content(mediaType = PROBLEM, schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Project not found",
                    content = @Content(mediaType = PROBLEM, schema = @Schema(implementation = ProblemDetail.class)))
    })
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