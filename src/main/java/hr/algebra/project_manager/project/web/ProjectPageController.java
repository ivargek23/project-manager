package hr.algebra.project_manager.project.web;

import hr.algebra.project_manager.common.WorkStatus;
import hr.algebra.project_manager.project.mapper.ProjectMapper;
import hr.algebra.project_manager.project.repository.ProjectSummary;
import hr.algebra.project_manager.project.service.ProjectService;
import hr.algebra.project_manager.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/projects")
@RequiredArgsConstructor
public class ProjectPageController {

    private static final int PAGE_SIZE = 10;
    private static final Sort BY_NAME = Sort.by("name");

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

        return "project/form";
    }

    @PostMapping
    public String createProject(@ModelAttribute ProjectForm form, Model model) {
        
    }

}
