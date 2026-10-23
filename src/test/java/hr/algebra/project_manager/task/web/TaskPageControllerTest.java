package hr.algebra.project_manager.task.web;

import hr.algebra.project_manager.common.WorkStatus;
import hr.algebra.project_manager.common.exception.NotFoundException;
import hr.algebra.project_manager.project.domain.Project;
import hr.algebra.project_manager.project.service.ProjectDetails;
import hr.algebra.project_manager.project.service.ProjectService;
import hr.algebra.project_manager.task.domain.Task;
import hr.algebra.project_manager.task.mapper.TaskMapperImpl;
import hr.algebra.project_manager.task.service.InvalidAssigneeException;
import hr.algebra.project_manager.task.service.SubtaskRow;
import hr.algebra.project_manager.task.service.TaskCommand;
import hr.algebra.project_manager.task.service.TaskService;
import hr.algebra.project_manager.user.domain.AppUser;
import hr.algebra.project_manager.user.domain.Role;
import hr.algebra.project_manager.user.repository.UserOption;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.validation.BindingResult;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
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

@WebMvcTest(TaskPageController.class)
@Import(TaskMapperImpl.class)
class TaskPageControllerTest {

    private static final String FORM_VIEW = "task/form";

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private TaskService taskService;

    @MockitoBean
    private ProjectService projectService;

    @BeforeEach
    void setUp() {
        when(projectService.getProjectDetails(1L)).thenReturn(new ProjectDetails(1L, "Website Redesign",
                null, LocalDate.of(2026, 10, 1), LocalDate.of(2026, 12, 15), WorkStatus.IN_PROGRESS, "Pero Peric"));
        when(taskService.findAssigneeOptions(1L)).thenReturn(List.of(new UserOption(10L, "Ana Horvat")));
        when(taskService.findSubtasks(1L, 5L)).thenReturn(List.of(new SubtaskRow(7L, "Write copy", false)));
    }

    // ---------------------------------------------------------------- task form

    @Test
    void newFormShowsEmptyFormWithoutSubtasks() throws Exception {
        mvc.perform(get("/projects/1/tasks/new"))
                .andExpect(status().isOk())
                .andExpect(view().name(FORM_VIEW))
                .andExpect(model().attributeExists("form", "projectName", "assignees", "statuses"))
                .andExpect(model().attributeDoesNotExist("subtasks", "subtaskForm"));
    }

    @Test
    void validTaskIsCreatedAndRedirectsToProject() throws Exception {
        mvc.perform(validTask(post("/projects/1/tasks")))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/projects/1"))
                .andExpect(flash().attribute("successMessage", "task.created"));

        verify(taskService).createTask(eq(1L), argThat((TaskCommand command) ->
                command.title().equals("Homepage wireframes") && command.estimateDays() == 5));
    }

    @Test
    void emptyFormIsShownAgainWithErrors() throws Exception {
        mvc.perform(post("/projects/1/tasks"))
                .andExpect(status().isOk())
                .andExpect(view().name(FORM_VIEW))
                .andExpect(model().attributeHasFieldErrors("form", "title", "estimateDays"));

        verify(taskService, never()).createTask(any(), any());
    }

    @Test
    void estimateOutsideOneToHundredIsRejected() throws Exception {
        mvc.perform(taskRequest(post("/projects/1/tasks"), "0"))
                .andExpect(view().name(FORM_VIEW))
                .andExpect(model().attributeHasFieldErrorCode("form", "estimateDays", "Min"));

        mvc.perform(taskRequest(post("/projects/1/tasks"), "101"))
                .andExpect(view().name(FORM_VIEW))
                .andExpect(model().attributeHasFieldErrorCode("form", "estimateDays", "Max"));

        verify(taskService, never()).createTask(any(), any());
    }

    @Test
    void invalidAssigneeBecomesAFieldError() throws Exception {
        when(taskService.createTask(eq(1L), any())).thenThrow(new InvalidAssigneeException(99L));

        mvc.perform(validTask(post("/projects/1/tasks")).param("assigneeId", "99"))
                .andExpect(view().name(FORM_VIEW))
                .andExpect(model().attributeHasFieldErrorCode("form", "assigneeId", "task.assignee.invalid"));
    }

    @Test
    void editFormShowsTaskAndItsSubtasks() throws Exception {
        when(taskService.get(1L, 5L)).thenReturn(task());

        mvc.perform(get("/projects/1/tasks/5/edit"))
                .andExpect(status().isOk())
                .andExpect(view().name(FORM_VIEW))
                .andExpect(model().attribute("taskId", 5L))
                .andExpect(model().attributeExists("subtasks", "subtaskForm"))
                .andExpect(result -> {
                    TaskForm form = (TaskForm) result.getModelAndView().getModel().get("form");
                    assertThat(form.getTitle()).isEqualTo("Homepage wireframes");
                    assertThat(form.getEstimateDays()).isEqualTo(5);
                });
    }

    @Test
    void taskOfAnotherProjectGives404() throws Exception {
        when(taskService.get(1L, 99L)).thenThrow(new NotFoundException("Task", 99L));

        mvc.perform(get("/projects/1/tasks/99/edit"))
                .andExpect(status().isNotFound());
    }

    @Test
    void validUpdateRedirectsToProject() throws Exception {
        mvc.perform(validTask(post("/projects/1/tasks/5")).param("version", "3"))
                .andExpect(redirectedUrl("/projects/1"))
                .andExpect(flash().attribute("successMessage", "task.updated"));

        verify(taskService).updateTask(eq(1L), eq(5L), eq(3L), any(TaskCommand.class));
    }

    @Test
    void staleVersionBecomesAFormError() throws Exception {
        doThrow(new ObjectOptimisticLockingFailureException(Task.class, 5L))
                .when(taskService).updateTask(eq(1L), eq(5L), eq(3L), any());

        mvc.perform(validTask(post("/projects/1/tasks/5")).param("version", "3"))
                .andExpect(view().name(FORM_VIEW))
                .andExpect(model().attribute("taskId", 5L))
                .andExpect(result -> {
                    BindingResult errors = (BindingResult) result.getModelAndView().getModel()
                            .get(BindingResult.MODEL_KEY_PREFIX + "form");
                    assertThat(errors.getGlobalError().getCode()).isEqualTo("task.stale");
                });
    }

    @Test
    void deleteRedirectsToProjectWithFlash() throws Exception {
        mvc.perform(post("/projects/1/tasks/5/delete"))
                .andExpect(redirectedUrl("/projects/1"))
                .andExpect(flash().attribute("successMessage", "task.deleted"));

        verify(taskService).deleteTask(1L, 5L);
    }

    @Test
    void validSubtaskIsAddedAndRedirectsToEditPage() throws Exception {
        mvc.perform(post("/projects/1/tasks/5/subtasks").param("title", "Write copy"))
                .andExpect(redirectedUrl("/projects/1/tasks/5/edit"));

        verify(taskService).addSubtask(1L, 5L, "Write copy");
    }

    @Test
    void emptySubtaskShowsTheErrorAndKeepsTheTaskForm() throws Exception {
        when(taskService.get(1L, 5L)).thenReturn(task());

        mvc.perform(post("/projects/1/tasks/5/subtasks").param("title", " "))
                .andExpect(status().isOk())
                .andExpect(view().name(FORM_VIEW))
                .andExpect(model().attributeHasFieldErrors("subtaskForm", "title"))
                .andExpect(model().attributeExists("form", "subtasks"));

        verify(taskService, never()).addSubtask(any(), any(), anyString());
    }

    @Test
    void subtaskIsMarkedDone() throws Exception {
        mvc.perform(post("/projects/1/tasks/5/subtasks/7/done").param("done", "true"))
                .andExpect(redirectedUrl("/projects/1/tasks/5/edit"));

        verify(taskService).setSubtaskDone(1L, 5L, 7L, true);
    }

    @Test
    void subtaskIsDeleted() throws Exception {
        mvc.perform(post("/projects/1/tasks/5/subtasks/7/delete"))
                .andExpect(redirectedUrl("/projects/1/tasks/5/edit"));

        verify(taskService).deleteSubtask(1L, 5L, 7L);
    }

    private static Task task() {
        AppUser lead = new AppUser("pperic", "Pero Peric", "hash", Role.PROJECT_LEAD);
        Project project = new Project("Website Redesign",
                LocalDate.of(2026, 10, 1), LocalDate.of(2026, 12, 15), lead);
        return new Task(project, "Homepage wireframes", 5);
    }

    private static MockHttpServletRequestBuilder validTask(MockHttpServletRequestBuilder request) {
        return taskRequest(request, "5");
    }

    private static MockHttpServletRequestBuilder taskRequest(MockHttpServletRequestBuilder request,
                                                             String estimateDays) {
        return request
                .param("title", "Homepage wireframes")
                .param("estimateDays", estimateDays)
                .param("status", "TODO");
    }
}