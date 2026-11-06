package hr.algebra.project_manager.project.api;

import hr.algebra.project_manager.common.WorkStatus;
import hr.algebra.project_manager.common.exception.NotFoundException;
import hr.algebra.project_manager.project.domain.Project;
import hr.algebra.project_manager.project.mapper.ProjectMapper;
import hr.algebra.project_manager.project.repository.ProjectSummary;
import hr.algebra.project_manager.project.service.InvalidLeadException;
import hr.algebra.project_manager.project.service.ProjectService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.endsWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProjectApiController.class)
@WithMockUser
class ProjectApiControllerTest {

    private static final ProjectResponse RESPONSE = new ProjectResponse(1L, "Website Redesign", "New site",
            LocalDate.of(2026, 10, 1), LocalDate.of(2026, 12, 15), WorkStatus.IN_PROGRESS, "Pero Peric", 0L);

    private static final String VALID_BODY = """
            {"name":"Website Redesign","startDate":"2026-10-01","dueDate":"2026-12-15","status":"IN_PROGRESS","version":0}
            """;

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private ProjectService projectService;

    @MockitoBean
    private ProjectMapper projectMapper;

    @Test
    void listReturnsPagedProjects() throws Exception {
        var summary = new ProjectSummary(1L, "Website Redesign", "Pero Peric",
                LocalDate.of(2026, 12, 15), WorkStatus.IN_PROGRESS, 20L);
        when(projectService.search(eq(""), isNull(), any()))
                .thenReturn(new PageImpl<>(List.of(summary), PageRequest.of(0, 10), 1));

        mvc.perform(get("/api/projects"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.page.totalElements").value(1));
    }

    @Test
    void listRejectsUnknownSortField() throws Exception {
        mvc.perform(get("/api/projects").param("sort", "passwordHash"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Invalid sort"));
    }

    @Test
    void getReturnsProject() throws Exception {
        when(projectMapper.toProjectResponse(any())).thenReturn(RESPONSE);

        mvc.perform(get("/api/projects/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.leadName").value("Pero Peric"));
    }

    @Test
    void getUnknownProjectReturns404Problem() throws Exception {
        when(projectService.get(99L)).thenThrow(new NotFoundException("Project", 99L));

        mvc.perform(get("/api/projects/99"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void createReturns201WithLocation() throws Exception {
        when(projectService.create(any())).thenReturn(1L);
        when(projectMapper.toProjectResponse(any())).thenReturn(RESPONSE);

        mvc.perform(post("/api/projects").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", endsWith("/api/projects/1")));
    }

    @Test
    void createWithBlankNameReturns400WithFieldError() throws Exception {
        mvc.perform(post("/api/projects").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"","startDate":"2026-10-01","dueDate":"2026-12-15","status":"TODO"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.name").exists());

        verifyNoInteractions(projectService);
    }

    @Test
    void createWithInvalidLeadReturns422() throws Exception {
        when(projectService.create(any())).thenThrow(new InvalidLeadException(5L));

        mvc.perform(post("/api/projects").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().is(422));
    }

    @Test
    void updateWithStaleVersionReturns409() throws Exception {
        doThrow(new ObjectOptimisticLockingFailureException(Project.class, 1L))
                .when(projectService).update(eq(1L), any(), any());

        mvc.perform(put("/api/projects/1").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isConflict());
    }

    @Test
    void deleteReturns204() throws Exception {
        mvc.perform(delete("/api/projects/1").with(csrf()))
                .andExpect(status().isNoContent());

        verify(projectService).delete(1L);
    }

    @Test
    void deleteWithoutPermissionReturns403Problem() throws Exception {
        doThrow(new AccessDeniedException("denied")).when(projectService).delete(1L);

        mvc.perform(delete("/api/projects/1").with(csrf()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.title").value("Forbidden"));
    }
}