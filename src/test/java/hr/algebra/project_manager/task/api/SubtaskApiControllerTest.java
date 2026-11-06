package hr.algebra.project_manager.task.api;

import hr.algebra.project_manager.common.exception.NotFoundException;
import hr.algebra.project_manager.task.service.SubtaskRow;
import hr.algebra.project_manager.task.service.TaskService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.endsWith;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SubtaskApiController.class)
@WithMockUser
class SubtaskApiControllerTest {

    private static final String BASE = "/api/projects/1/tasks/10/subtasks";

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private TaskService taskService;

    @Test
    void listReturnsSubtasks() throws Exception {
        when(taskService.findSubtasks(1L, 10L)).thenReturn(List.of(
                new SubtaskRow(100L, "Desktop layout", true),
                new SubtaskRow(101L, "Mobile layout", false)));

        mvc.perform(get(BASE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].title").value("Desktop layout"));
    }

    @Test
    void getUnknownSubtaskReturns404() throws Exception {
        when(taskService.getSubtask(1L, 10L, 999L)).thenThrow(new NotFoundException("Subtask", 999L));

        mvc.perform(get(BASE + "/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void addReturns201WithLocation() throws Exception {
        when(taskService.addSubtask(1L, 10L, "Footer")).thenReturn(102L);
        when(taskService.getSubtask(1L, 10L, 102L)).thenReturn(new SubtaskRow(102L, "Footer", false));

        mvc.perform(post(BASE).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content("""
                                {"title":"Footer"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", endsWith(BASE + "/102")))
                .andExpect(jsonPath("$.done").value(false));
    }

    @Test
    void addWithBlankTitleReturns400WithFieldError() throws Exception {
        mvc.perform(post(BASE).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content("""
                                {"title":"  "}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.title").exists());

        verifyNoInteractions(taskService);
    }

    @Test
    void addByNonAssigneeReturns403Problem() throws Exception {
        when(taskService.addSubtask(1L, 10L, "Footer")).thenThrow(new AccessDeniedException("denied"));

        mvc.perform(post(BASE).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content("""
                                {"title":"Footer"}
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.title").value("Forbidden"));
    }

    @Test
    void patchDoneReturns204() throws Exception {
        mvc.perform(patch(BASE + "/100").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content("""
                                {"done":true}
                                """))
                .andExpect(status().isNoContent());

        verify(taskService).setSubtaskDone(1L, 10L, 100L, true);
    }

    @Test
    void patchWithoutDoneReturns400() throws Exception {
        mvc.perform(patch(BASE + "/100").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.done").exists());

        verifyNoInteractions(taskService);
    }

    @Test
    void deleteReturns204() throws Exception {
        mvc.perform(delete(BASE + "/100").with(csrf()))
                .andExpect(status().isNoContent());

        verify(taskService).deleteSubtask(1L, 10L, 100L);
    }
}