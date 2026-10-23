package hr.algebra.project_manager.task.web;

import hr.algebra.project_manager.common.WorkStatus;
import hr.algebra.project_manager.project.service.ProjectService;
import hr.algebra.project_manager.task.mapper.TaskMapper;
import hr.algebra.project_manager.task.service.InvalidAssigneeException;
import hr.algebra.project_manager.task.service.SubtaskForm;
import hr.algebra.project_manager.task.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/projects/{projectId}/tasks")
@RequiredArgsConstructor
public class TaskPageController {

    private final TaskService taskService;
    private final ProjectService projectService;
    private final TaskMapper taskMapper;

    private static final String TASK_FORM_VIEW = "task/form";
    private static final String REDIRECT_TO_PROJECT = "redirect:/projects/";
    private static final String MESSAGE_TYPE_SUCCESS = "successMessage";

    @GetMapping("/new")
    public String newTaskForm(@PathVariable Long projectId, Model model) {
        model.addAttribute("form", new TaskForm());
        return showForm(model, projectId, null);
    }

    @PostMapping
    public String createTask(@PathVariable Long projectId,
                             @Valid @ModelAttribute("form") TaskForm form,
                             BindingResult result,
                             Model model,
                             RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return showForm(model, projectId, null);
        }

       try {
           taskService.createTask(projectId, taskMapper.toCommand(form));
           redirectAttributes.addFlashAttribute(MESSAGE_TYPE_SUCCESS, "task.created");
           return REDIRECT_TO_PROJECT + projectId;
       } catch (InvalidAssigneeException _) {
           result.rejectValue("assigneeId", "task.assignee.invalid");
           return showForm(model, projectId, null);
       }
    }

    @GetMapping("/{taskId}/edit")
    public String getEditTaskForm(@PathVariable Long projectId,
                                  @PathVariable Long taskId,
                                  Model model) {

        model.addAttribute("form", taskMapper.toForm(taskService.get(projectId, taskId)));
        return showForm(model, projectId, taskId);
    }

    @PostMapping("/{taskId}")
    public String updateTask(@PathVariable Long projectId,
                             @PathVariable Long taskId,
                             @Valid @ModelAttribute("form") TaskForm form,
                             BindingResult result,
                             Model model,
                             RedirectAttributes attributes) {
        if (result.hasErrors()) {
            return showForm(model, projectId, taskId);
        }
        try {
            taskService.updateTask(projectId, taskId, form.getVersion(), taskMapper.toCommand(form));
            attributes.addFlashAttribute(MESSAGE_TYPE_SUCCESS, "task.updated");
            return REDIRECT_TO_PROJECT + projectId;
        } catch (OptimisticLockingFailureException _) {
            result.reject("task.stale");
            return showForm(model, projectId, taskId);
        } catch (InvalidAssigneeException _) {
            result.rejectValue("assigneeId", "task.assignee.invalid");
            return showForm(model, projectId, taskId);
        }
    }

    @PostMapping("/{taskId}/delete")
    public String deleteTask(@PathVariable Long projectId,
                             @PathVariable Long taskId,
                             RedirectAttributes attributes) {
        taskService.deleteTask(projectId, taskId);
        attributes.addFlashAttribute(MESSAGE_TYPE_SUCCESS, "task.deleted");
        return REDIRECT_TO_PROJECT + projectId;
    }

    @PostMapping("/{taskId}/subtasks")
    public String addSubtask(@Valid @ModelAttribute("subtaskForm") SubtaskForm subtaskForm,
                                 BindingResult result, Model model,
                                 @PathVariable Long projectId,
                                 @PathVariable Long taskId) {
        if (result.hasErrors()) {
            model.addAttribute("form", taskMapper.toForm(taskService.get(projectId, taskId)));
            return showForm(model, projectId, taskId);
        }
        taskService.addSubtask(projectId, taskId, subtaskForm.getTitle());
        return redirectToEdit(projectId, taskId);
    }

    @PostMapping("/{taskId}/subtasks/{subtaskId}/done")
    public String setSubtaskDone(@PathVariable Long projectId,
                                   @PathVariable Long taskId,
                                   @PathVariable Long subtaskId,
                                  @RequestParam boolean done) {
        taskService.setSubtaskDone(projectId, taskId, subtaskId, done);
        return redirectToEdit(projectId, taskId);
    }

    @PostMapping("/{taskId}/subtasks/{subtaskId}/delete")
    public String deleteSubtask(@PathVariable Long projectId,
                                 @PathVariable Long taskId,
                                 @PathVariable Long subtaskId) {
        taskService.deleteSubtask(projectId, taskId, subtaskId);
        return redirectToEdit(projectId, taskId);
    }

    private String redirectToEdit(Long projectId, Long taskId) {
        String editPath = projectId + "/tasks/" + taskId + "/edit";
        return REDIRECT_TO_PROJECT.concat(editPath);
    }

    private String showForm(Model model, Long projectId, Long taskId) {
        model.addAttribute("projectId", projectId);
        model.addAttribute("taskId", taskId);
        if (taskId != null) {
            model.addAttribute("subtasks", taskService.findSubtasks(projectId, taskId));
            if (!model.containsAttribute("subtaskForm")) {
                model.addAttribute("subtaskForm", new SubtaskForm());
            }
        }
        model.addAttribute("projectName", projectService.getProjectDetails(projectId).name());
        model.addAttribute("assignees", taskService.findAssigneeOptions(projectId));
        model.addAttribute("statuses", WorkStatus.values());
        return TASK_FORM_VIEW;
    }
}
