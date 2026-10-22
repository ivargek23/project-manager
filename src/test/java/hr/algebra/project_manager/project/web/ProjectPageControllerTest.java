package hr.algebra.project_manager.project.web;

import hr.algebra.project_manager.common.WorkStatus;
import hr.algebra.project_manager.common.exception.NotFoundException;
import hr.algebra.project_manager.project.mapper.ProjectMapperImpl;
import hr.algebra.project_manager.project.repository.ProjectSummary;
import hr.algebra.project_manager.project.service.InvalidLeadException;
import hr.algebra.project_manager.project.service.ProjectCommand;
import hr.algebra.project_manager.project.service.ProjectDetails;
import hr.algebra.project_manager.project.service.ProjectService;
import hr.algebra.project_manager.task.service.TaskService;
import hr.algebra.project_manager.user.repository.UserOption;
import hr.algebra.project_manager.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.validation.BindingResult;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(ProjectPageController.class)
@Import(ProjectMapperImpl.class)
class ProjectPageControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private ProjectService projectService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private TaskService taskService;

    @BeforeEach
    void setUp() {
        when(userService.findLeadCandidates()).thenReturn(List.of(new UserOption(2L, "Michael Chen")));
    }

    @Test
    void listShowsProjects() throws Exception {
        var summary = new ProjectSummary(1L, "Website Redesign", "Michael Chen",
                LocalDate.of(2026, 12, 15), WorkStatus.IN_PROGRESS, 20L);
        when(projectService.search(eq("web"), eq(null), any()))
                .thenReturn(new PageImpl<>(List.of(summary), PageRequest.of(0, 10), 1));

        mvc.perform(get("/projects").param("q", "web"))
                .andExpect(status().isOk())
                .andExpect(view().name("project/list"))
                .andExpect(model().attribute("q", "web"));
    }

    @Test
    void newFormShowsEmptyForm() throws Exception {
        mvc.perform(get("/projects/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("project/form"))
                .andExpect(model().attributeExists("form", "leads", "statuses"));
    }

    @Test
    void validProjectIsCreatedAndRedirectsToDetail() throws Exception {
        when(projectService.create(any(ProjectCommand.class))).thenReturn(42L);

        mvc.perform(validProject(post("/projects")))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/projects/42"))
                .andExpect(flash().attribute("successMessage", "project.created"));
    }

    @Test
    void emptyFormIsShownAgainWithErrors() throws Exception {
        mvc.perform(post("/projects"))
                .andExpect(status().isOk())
                .andExpect(view().name("project/form"))
                .andExpect(model().attributeHasFieldErrors("form", "name", "startDate", "dueDate", "leadId"));

        verify(projectService, never()).create(any());
    }

    @Test
    void dueDateBeforeStartIsReportedOnDueDate() throws Exception {
        mvc.perform(post("/projects")
                        .param("name", "Website Redesign")
                        .param("startDate", "2026-10-01")
                        .param("dueDate", "2026-09-01")
                        .param("leadId", "2")
                        .param("status", "TODO"))
                .andExpect(view().name("project/form"))
                .andExpect(model().attributeHasFieldErrorCode("form", "dueDate", "ValidDateRange"));

        verify(projectService, never()).create(any());
    }

    @Test
    void invalidLeadBecomesAFieldError() throws Exception {
        when(projectService.create(any())).thenThrow(new InvalidLeadException(2L));

        mvc.perform(validProject(post("/projects")))
                .andExpect(view().name("project/form"))
                .andExpect(model().attributeHasFieldErrorCode("form", "leadId", "project.lead.invalid"));
    }

    @Test
    void staleVersionBecomesAFormError() throws Exception {
        doThrow(new ObjectOptimisticLockingFailureException("Project", 1L))
                .when(projectService).update(eq(1L), anyLong(), any());

        mvc.perform(validProject(post("/projects/1")).param("version", "3"))
                .andExpect(view().name("project/form"))
                .andExpect(model().attribute("projectId", 1L))
                .andExpect(result -> {
                    BindingResult errors = (BindingResult) result.getModelAndView().getModel()
                            .get(BindingResult.MODEL_KEY_PREFIX + "form");
                    assertThat(errors.getGlobalError().getCode()).isEqualTo("project.stale");
                });
    }

    @Test
    void detailShowsProjectAndTasks() throws Exception {
        when(projectService.getProjectDetails(1L)).thenReturn(new ProjectDetails(1L, "Website Redesign",
                null, LocalDate.of(2026, 10, 1), LocalDate.of(2026, 12, 15), WorkStatus.IN_PROGRESS, "Michael Chen"));
        when(taskService.findRows(1L)).thenReturn(List.of());

        mvc.perform(get("/projects/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("project/detail"))
                .andExpect(model().attribute("doneCount", 0L));
    }

    @Test
    void unknownProjectGives404() throws Exception {
        when(projectService.getProjectDetails(99L)).thenThrow(new NotFoundException("Project", 99L));

        mvc.perform(get("/projects/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteRedirectsToListWithFlash() throws Exception {
        mvc.perform(post("/projects/1/delete"))
                .andExpect(redirectedUrl("/projects"))
                .andExpect(flash().attribute("successMessage", "project.deleted"));

        verify(projectService).delete(1L);
    }

    private static org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder validProject(
            org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder request) {
        return request
                .param("name", "Website Redesign")
                .param("startDate", "2026-10-01")
                .param("dueDate", "2026-12-15")
                .param("leadId", "2")
                .param("status", "TODO");
    }
}