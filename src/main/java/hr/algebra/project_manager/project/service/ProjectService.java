package hr.algebra.project_manager.project.service;

import hr.algebra.project_manager.common.WorkStatus;
import hr.algebra.project_manager.common.exception.NotFoundException;
import hr.algebra.project_manager.project.domain.Project;
import hr.algebra.project_manager.project.repository.ProjectRepository;
import hr.algebra.project_manager.project.repository.ProjectSummary;
import hr.algebra.project_manager.security.AppUserPrincipal;
import hr.algebra.project_manager.user.domain.AppUser;
import hr.algebra.project_manager.user.domain.Role;
import hr.algebra.project_manager.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProjectService {

    private final ProjectRepository projects;
    private final UserRepository users;

    @PreAuthorize("isAuthenticated()")
    public Page<ProjectSummary> search(String query, WorkStatus status, Pageable pageable) {
        String q = query == null ? "" : query.strip();

        AppUserPrincipal principal = getCurrentPrincipal();

        if (principal.role() == Role.ADMIN) return projects.search(q, status, pageable);

        return projects.searchVisibleTo(principal.id(), q, status, pageable);
    }

    @PreAuthorize("hasRole('ADMIN') or @projectAccess.isLead(#id, authentication) or @projectAccess.isMember(#id, authentication)")
    public Project get(Long id) {
        return projects.findWithLeadById(id)
                .orElseThrow(() -> new NotFoundException("Project", id));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_LEAD')")
    @Transactional
    public Long create(ProjectCommand command) {
        AppUserPrincipal current = getCurrentPrincipal();

        Long leadId = current.role() == Role.ADMIN ? command.leadId() : current.id();
        AppUser lead = findLead(leadId);
        Project project = new Project(command.name(), command.startDate(), command.dueDate(),
                lead);
        project.setDescription(command.description());
        project.setStatus(command.status());
        return projects.save(project).getId();
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN') or @projectAccess.isLead(#id, authentication)")
    public void update(Long id, Long expectedVersion, ProjectCommand command) {
        Project project = get(id);
        AppUserPrincipal principal = getCurrentPrincipal();

        if (!Objects.equals(project.getVersion(), expectedVersion)) {
            throw new ObjectOptimisticLockingFailureException(Project.class, id);
        }
        project.setName(command.name());
        project.setDescription(command.description());
        project.setStartDate(command.startDate());
        project.setDueDate(command.dueDate());
        project.setStatus(command.status());
        if (principal.role() == Role.ADMIN) {
            project.setLead(findLead(command.leadId()));
        }
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN') or @projectAccess.isLead(#id, authentication)")
    public void delete(Long id) {
        Project project = projects.findById(id)
                .orElseThrow(() -> new NotFoundException("Project", id));
        projects.delete(project);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN') or @projectAccess.isLead(#id, authentication) or @projectAccess.isMember(#id, authentication)")
    public ProjectDetails getProjectDetails(Long id) {
        Project project = get(id);
        AppUser lead = project.getLead();

        return new ProjectDetails(
                project.getId(),
                project.getName(),
                project.getDescription(),
                project.getStartDate(),
                project.getDueDate(),
                project.getStatus(),
                lead == null ? null : lead.getDisplayName()
        );
    }

    private AppUser findLead(Long leadId) {
        AppUser lead = users.findById(leadId)
                .orElseThrow(() -> new NotFoundException("User", leadId));
        if (!lead.isEnabled() || !Role.CAN_BE_A_PROJECT_LEAD.contains(lead.getRole())) {
            throw new InvalidLeadException(leadId);
        }
        return lead;
    }

    private AppUserPrincipal getCurrentPrincipal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof AppUserPrincipal principal) {
            return principal;
        }
        throw new IllegalStateException("No authenticated user in the security context");
    }
}
