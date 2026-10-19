package hr.algebra.project_manager.project.service;

import hr.algebra.project_manager.common.exception.NotFoundException;
import hr.algebra.project_manager.common.WorkStatus;
import hr.algebra.project_manager.project.domain.Project;
import hr.algebra.project_manager.project.repository.ProjectRepository;
import hr.algebra.project_manager.project.repository.ProjectSummary;
import hr.algebra.project_manager.user.domain.AppUser;
import hr.algebra.project_manager.user.domain.Role;
import hr.algebra.project_manager.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProjectService {

    private final ProjectRepository projects;
    private final UserRepository users;

    public Page<ProjectSummary> search(String query, WorkStatus status, Pageable pageable) {
        String text = query == null ? "" : query.strip();
        return projects.search(text, status, pageable);
    }

    public Project get(Long id) {
        return projects.findWithLeadById(id)
                .orElseThrow(() -> new NotFoundException("Project", id));
    }

    @Transactional
    public Long create(ProjectCommand command) {
        Project project = new Project(command.name(), command.startDate(), command.dueDate(),
                findLead(command.leadId()));
        project.setDescription(command.description());
        project.setStatus(command.status());
        return projects.save(project).getId();
    }

    @Transactional
    public void update(Long id, Long expectedVersion, ProjectCommand command) {
        Project project = get(id);
        if (!Objects.equals(project.getVersion(), expectedVersion)) {
            throw new ObjectOptimisticLockingFailureException(Project.class, id);
        }
        project.setName(command.name());
        project.setDescription(command.description());
        project.setStartDate(command.startDate());
        project.setDueDate(command.dueDate());
        project.setStatus(command.status());
        project.setLead(findLead(command.leadId()));
        // no save(): the project is managed, so Hibernate writes the changes at commit
    }

    @Transactional
    public void delete(Long id) {
        Project project = projects.findById(id)
                .orElseThrow(() -> new NotFoundException("Project", id));
        projects.delete(project);
    }

    private AppUser findLead(Long leadId) {
        AppUser lead = users.findById(leadId)
                .orElseThrow(() -> new NotFoundException("User", leadId));
        if (!lead.isEnabled() || !Role.CAN_BE_A_PROJECT_LEAD.contains(lead.getRole())) {
            throw new IllegalArgumentException("User " + leadId + " can't lead a project");
        }
        return lead;
    }
}
