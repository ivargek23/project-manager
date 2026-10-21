package hr.algebra.project_manager.project.web;

import hr.algebra.project_manager.common.WorkStatus;
import hr.algebra.project_manager.project.mapper.ProjectMapper;
import hr.algebra.project_manager.project.repository.ProjectSummary;
import hr.algebra.project_manager.project.service.ProjectService;
import hr.algebra.project_manager.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/projects")
@RequiredArgsConstructor
public class ProjectPageController {

    private static final int PAGE_SIZE = 10;
    private static final Sort BY_NAME = Sort.by("name");
    private static final String FORM_VIEW = "project/form";

    private final ProjectService projectService;
    private final UserService userService;
    private final ProjectMapper projectMapper;

    @GetMapping
    public String getProjects(@RequestParam(defaultValue = "") String q,
                              @RequestParam(required = false) WorkStatus status,
                              @RequestParam(defaultValue = "0") int page,
                              Model model) {
        PageRequest request = PageRequest.of(Math.max(page, 0), PAGE_SIZE, BY_NAME);

        Page<ProjectSummary> projects = projectService.search(q, status, request);

        model.addAttribute("projects", projects);
        model.addAttribute("statuses", WorkStatus.values());
        model.addAttribute("q", q);
        model.addAttribute("status", status);

        return "project/list";
    }

    @GetMapping("/new")
    public String getNewForm(Model model) {
        model.addAttribute("form", new ProjectForm());
        addFormOptions(model);

        return FORM_VIEW;
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("form") ProjectForm form,
                         BindingResult result,
                         Model model,
                         RedirectAttributes attributes) {
        if (result.hasErrors()) {
            return showForm(model, null);
        }
        try {
            projectService.create(projectMapper.toCommand(form));
            attributes.addFlashAttribute("successMessage", "project.created");
            return "redirect:/projects";
        } catch (IllegalArgumentException e) {
            result.rejectValue("leadId", "project.lead.invalid");
            return showForm(model, null);
        }
    }

    @GetMapping("/{id}/edit")
    public String getEditForm(@PathVariable Long id,
                              Model model) {
        model.addAttribute("form", projectMapper.toForm(projectService.get(id)));
        model.addAttribute("projectId", id);
        addFormOptions(model);
        return FORM_VIEW;
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("form") ProjectForm form,
                         BindingResult result,
                         Model model,
                         RedirectAttributes attributes) {
        if (result.hasErrors()) {
            return showForm(model, id);
        }
        try {
            projectService.update(id, form.getVersion(), projectMapper.toCommand(form));
            attributes.addFlashAttribute("successMessage", "project.updated");
            return "redirect:/projects";
        } catch (OptimisticLockingFailureException e) {
            result.reject("project.stale");
            return showForm(model, id);
        } catch (IllegalArgumentException e) {
            result.rejectValue("leadId", "project.lead.invalid");
            return showForm(model, id);
        }
    }

    private void addFormOptions(Model model) {
        model.addAttribute("leads", userService.findLeadCandidates());
        model.addAttribute("statuses", WorkStatus.values());
    }

    private String showForm(Model model, Long projectId) {
        model.addAttribute("projectId", projectId);
        addFormOptions(model);
        return FORM_VIEW;
    }
}
