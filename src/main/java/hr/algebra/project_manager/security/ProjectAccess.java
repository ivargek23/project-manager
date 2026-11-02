package hr.algebra.project_manager.security;

import hr.algebra.project_manager.project.repository.ProjectMemberRepository;
import hr.algebra.project_manager.project.repository.ProjectRepository;
import hr.algebra.project_manager.task.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component("projectAccess")
@RequiredArgsConstructor
public class ProjectAccess {

    private final ProjectMemberRepository projectMemberRepository;
    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;

    public boolean isLead(Long projectId, Authentication authentication) {
        if (!(authentication.getPrincipal() instanceof AppUserPrincipal principal)) return false;

        return projectRepository.existsByIdAndLeadId(projectId, principal.id());
    }

    public boolean isMember(Long projectId, Authentication authentication) {
        if (!(authentication.getPrincipal() instanceof AppUserPrincipal principal)) return false;

        return projectMemberRepository.existsByProjectIdAndUserId(projectId, principal.id());
    }

    public boolean isAssignee(Long projectId, Long taskId, Authentication authentication) {
        if (!(authentication.getPrincipal() instanceof AppUserPrincipal principal)) return false;

        return taskRepository.existsByIdAndProjectIdAndAssigneeId(taskId, projectId, principal.id());
    }
}
