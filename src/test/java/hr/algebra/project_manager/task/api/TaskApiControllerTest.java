package hr.algebra.project_manager.task.api;

import hr.algebra.project_manager.common.WorkStatus;
import hr.algebra.project_manager.common.exception.NotFoundException;
import hr.algebra.project_manager.task.domain.Task;
import hr.algebra.project_manager.task.mapper.TaskMapper;
import hr.algebra.project_manager.task.service.InvalidAssigneeException;
import hr.algebra.project_manager.task.service.TaskService;
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
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TaskApiController.class)
@WithMockUser
class TaskApiControllerTest {

    private static final TaskResponse RESPONSE = new TaskResponse(10L, "Homepage wireframes", "Desktop and mobile",
            WorkStatus.IN_PROGRESS, 5, LocalDate.of(2026, 11, 20), 4L, "Ana Anic", 0L);

    private static final String VALID_BODY = """
            {"title":"Homepage wireframes","status":"IN_PROGRESS","estimateDays":5,"assigneeId":4,"version":0}
            """;

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private TaskService taskService;

    @MockitoBean
    private TaskMapper taskMapper;

    @Test
    void listReturnsPagedTasks() throws Exception {
        when(taskService.findPage(eq(1L), any()))
                .thenReturn(new PageImpl<>(List.of(mock(Task.class)), PageRequest.of(0, 20), 1));
        when(taskMapper.toTaskResponse(any())).thenReturn(RESPONSE);

        mvc.perform(get("/api/projects/1/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value("Homepage wireframes"))
                .andExpect(jsonPath("$.page.totalElements").value(1));
    }

    @Test
    void listRejectsUnknownSortField() throws Exception {
        mvc.perform(get("/api/projects/1/tasks").param("sort", "assignee.passwordHash"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Invalid sort"));
    }

    @Test
    void getReturnsTask() throws Exception {
        when(taskMapper.toTaskResponse(any())).thenReturn(RESPONSE);

        mvc.perform(get("/api/projects/1/tasks/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.assigneeName").value("Ana Anic"));
    }

    @Test
    void getTaskFromAnotherProjectReturns404() throws Exception {
        when(taskService.get(1L, 99L)).thenThrow(new NotFoundException("Task", 99L));

        mvc.perform(get("/api/projects/1/tasks/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void createReturns201WithLocation() throws Exception {
        when(taskService.createTask(eq(1L), any())).thenReturn(10L);
        when(taskMapper.toTaskResponse(any())).thenReturn(RESPONSE);

        mvc.perform(post("/api/projects/1/tasks").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", endsWith("/api/projects/1/tasks/10")));
    }

    @Test
    void createWithZeroEstimateReturns400WithFieldError() throws Exception {
        mvc.perform(post("/api/projects/1/tasks").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Homepage wireframes","status":"TODO","estimateDays":0}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.estimateDays").exists());

        verifyNoInteractions(taskService);
    }

    @Test
    void createWithAssigneeOutsideProjectReturns422() throws Exception {
        when(taskService.createTask(eq(1L), any())).thenThrow(new InvalidAssigneeException(4L));

        mvc.perform(post("/api/projects/1/tasks").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().is(422));
    }

    @Test
    void updateWithStaleVersionReturns409() throws Exception {
        doThrow(new ObjectOptimisticLockingFailureException(Task.class, 10L))
                .when(taskService).updateTask(eq(1L), eq(10L), any(), any());

        mvc.perform(put("/api/projects/1/tasks/10").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isConflict());
    }

    @Test
    void patchStatusUpdatesStatusAndReturnsTask() throws Exception {
        when(taskMapper.toTaskResponse(any())).thenReturn(RESPONSE);

        mvc.perform(patch("/api/projects/1/tasks/10/status").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content("""
                                {"status":"DONE"}
                                """))
                .andExpect(status().isOk());

        verify(taskService).updateStatus(1L, 10L, WorkStatus.DONE);
    }

    @Test
    void patchStatusByNonAssigneeReturns403Problem() throws Exception {
        doThrow(new AccessDeniedException("denied"))
                .when(taskService).updateStatus(1L, 10L, WorkStatus.DONE);

        mvc.perform(patch("/api/projects/1/tasks/10/status").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content("""
                                {"status":"DONE"}
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.title").value("Forbidden"));
    }

    @Test
    void deleteReturns204() throws Exception {
        mvc.perform(delete("/api/projects/1/tasks/10").with(csrf()))
                .andExpect(status().isNoContent());

        verify(taskService).deleteTask(1L, 10L);
    }
}