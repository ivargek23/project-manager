package hr.algebra.project_manager.dependency.api;

import hr.algebra.project_manager.dependency.repository.DependencyRow;
import hr.algebra.project_manager.dependency.service.DependencyService;
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
@RequestMapping("/api/projects/{projectId}/dependencies")
@RequiredArgsConstructor
@Tag(name = "Dependencies", description = "Finish-to-start links between tasks of one project")
public class DependencyApiController {

    private static final String PROBLEM = MediaType.APPLICATION_PROBLEM_JSON_VALUE;

    private final DependencyService dependencyService;

    @GetMapping
    @Operation(summary = "List dependencies", description = "All dependencies of the project, with task titles")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Dependencies of the project"),
            @ApiResponse(responseCode = "403", description = "Not the lead or a member of this project",
                    content = @Content(mediaType = PROBLEM, schema = @Schema(implementation = ProblemDetail.class)))
    })
    public List<DependencyRow> getDependencies(@PathVariable Long projectId) {
        return dependencyService.findDependencies(projectId);
    }

    @GetMapping("/{dependencyId}")
    @Operation(summary = "Get a dependency")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "The dependency"),
            @ApiResponse(responseCode = "403", description = "Not the lead or a member of this project",
                    content = @Content(mediaType = PROBLEM, schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Dependency not found in this project",
                    content = @Content(mediaType = PROBLEM, schema = @Schema(implementation = ProblemDetail.class)))
    })
    public DependencyRow getDependency(@PathVariable Long projectId, @PathVariable Long dependencyId) {
        return dependencyService.getDependency(projectId, dependencyId);
    }

    @PostMapping
    @Operation(summary = "Add a dependency",
            description = "Adds predecessor → successor. Rejected with 422 if it would create a cycle; "
                    + "the ProblemDetail then has a 'cycle' property with the task titles of the loop.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Dependency created; Location header points to it"),
            @ApiResponse(responseCode = "400", description = "Validation failed (see errors)",
                    content = @Content(mediaType = PROBLEM, schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "403", description = "Only the lead of this project",
                    content = @Content(mediaType = PROBLEM, schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "A task was not found in this project",
                    content = @Content(mediaType = PROBLEM, schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "409", description = "This dependency already exists",
                    content = @Content(mediaType = PROBLEM, schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "422", description = "The link would create a cycle, or a task would depend on itself",
                    content = @Content(mediaType = PROBLEM, schema = @Schema(implementation = ProblemDetail.class)))
    })
    public ResponseEntity<DependencyRow> addDependency(@PathVariable Long projectId,
                                                       @Valid @RequestBody DependencyRequest request) {
        Long dependencyId = dependencyService.addDependency(
                projectId, request.predecessorId(), request.successorId(), request.lagDays());
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{dependencyId}")
                .buildAndExpand(dependencyId)
                .toUri();
        return ResponseEntity.created(location).body(dependencyService.getDependency(projectId, dependencyId));
    }

    @PutMapping("/{dependencyId}")
    @Operation(summary = "Update a dependency",
            description = "Changes the tasks or the lag. Changing only the lag skips the cycle check.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "The updated dependency"),
            @ApiResponse(responseCode = "400", description = "Validation failed (see errors)",
                    content = @Content(mediaType = PROBLEM, schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "403", description = "Only the lead of this project",
                    content = @Content(mediaType = PROBLEM, schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Dependency or task not found in this project",
                    content = @Content(mediaType = PROBLEM, schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "409", description = "Another dependency already links these tasks",
                    content = @Content(mediaType = PROBLEM, schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "422", description = "The link would create a cycle, or a task would depend on itself",
                    content = @Content(mediaType = PROBLEM, schema = @Schema(implementation = ProblemDetail.class)))
    })
    public DependencyRow updateDependency(@PathVariable Long projectId, @PathVariable Long dependencyId,
                                          @Valid @RequestBody DependencyRequest request) {
        dependencyService.updateDependency(projectId, dependencyId,
                request.predecessorId(), request.successorId(), request.lagDays());
        return dependencyService.getDependency(projectId, dependencyId);
    }

    @DeleteMapping("/{dependencyId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete a dependency")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Dependency deleted"),
            @ApiResponse(responseCode = "403", description = "Only the lead of this project",
                    content = @Content(mediaType = PROBLEM, schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Dependency not found in this project",
                    content = @Content(mediaType = PROBLEM, schema = @Schema(implementation = ProblemDetail.class)))
    })
    public void deleteDependency(@PathVariable Long projectId, @PathVariable Long dependencyId) {
        dependencyService.deleteDependency(projectId, dependencyId);
    }
}