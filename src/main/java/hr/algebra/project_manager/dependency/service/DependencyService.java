package hr.algebra.project_manager.dependency.service;

import hr.algebra.project_manager.common.exception.NotFoundException;
import hr.algebra.project_manager.dependency.domain.TaskDependency;
import hr.algebra.project_manager.dependency.repository.DependencyRow;
import hr.algebra.project_manager.dependency.repository.GraphQueryRepository;
import hr.algebra.project_manager.dependency.repository.TaskDependencyRepository;
import hr.algebra.project_manager.project.domain.Project;
import hr.algebra.project_manager.project.repository.ProjectRepository;
import hr.algebra.project_manager.task.domain.Task;
import hr.algebra.project_manager.task.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DependencyService {

    private static final long NO_EXCLUSION = -1L;

    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;
    private final TaskDependencyRepository dependencyRepository;
    private final GraphQueryRepository graphQueryRepository;

    @Transactional
    @PreAuthorize("@projectAccess.isLead(#projectId, authentication)")
    public Long addDependency(Long projectId, Long predecessorId, Long successorId, int lagDays) {
        Project project = lockProject(projectId);
        rejectSelfLink(predecessorId, successorId);
        Task predecessor = findTask(projectId, predecessorId);
        Task successor = findTask(projectId, successorId);

        if (dependencyRepository.existsByPredecessorIdAndSuccessorId(predecessorId, successorId)) {
            throw new DuplicateDependencyException(predecessorId, successorId);
        }
        rejectCycle(projectId, predecessorId, successorId, NO_EXCLUSION);

        TaskDependency dependency = dependencyRepository.save(new TaskDependency(predecessor, successor, lagDays));
        project.bumpScheduleVersion();
        return dependency.getId();
    }

    @Transactional
    @PreAuthorize("@projectAccess.isLead(#projectId, authentication)")
    public void updateDependency(Long projectId, Long dependencyId,
                                 Long predecessorId, Long successorId, int lagDays) {
        Project project = lockProject(projectId);
        TaskDependency dependency = findDependency(projectId, dependencyId);

        boolean tasksChanged = !dependency.getPredecessor().getId().equals(predecessorId)
                || !dependency.getSuccessor().getId().equals(successorId);

        if (tasksChanged) {
            rejectSelfLink(predecessorId, successorId);
            if (dependencyRepository.existsByPredecessorIdAndSuccessorIdAndIdNot(predecessorId, successorId, dependencyId)) {
                throw new DuplicateDependencyException(predecessorId, successorId);
            }
            rejectCycle(projectId, predecessorId, successorId, dependencyId);
            dependency.relink(findTask(projectId, predecessorId), findTask(projectId, successorId));
        }

        dependency.setLagDays(lagDays);
        project.bumpScheduleVersion();
    }

    @Transactional
    @PreAuthorize("@projectAccess.isLead(#projectId, authentication)")
    public void deleteDependency(Long projectId, Long dependencyId) {
        Project project = lockProject(projectId);
        dependencyRepository.delete(findDependency(projectId, dependencyId));
        project.bumpScheduleVersion();
    }

    @PreAuthorize("hasRole('ADMIN') or @projectAccess.isLead(#projectId, authentication) or @projectAccess.isMember(#projectId, authentication)")
    public List<DependencyRow> findDependencies(Long projectId) {
        return dependencyRepository.findRows(projectId);
    }

    @PreAuthorize("hasRole('ADMIN') or @projectAccess.isLead(#projectId, authentication) or @projectAccess.isMember(#projectId, authentication)")
    public DependencyRow getDependency(Long projectId, Long dependencyId) {
        return dependencyRepository.findRow(projectId, dependencyId)
                .orElseThrow(() -> new NotFoundException("Dependency", dependencyId));
    }

    private Project lockProject(Long projectId) {
        return projectRepository.findByIdForUpdate(projectId)
                .orElseThrow(() -> new NotFoundException("Project", projectId));
    }

    private Task findTask(Long projectId, Long taskId) {
        return taskRepository.findByIdAndProjectId(taskId, projectId)
                .orElseThrow(() -> new NotFoundException("Task", taskId));
    }

    private TaskDependency findDependency(Long projectId, Long dependencyId) {
        return dependencyRepository.findByIdAndProjectId(dependencyId, projectId)
                .orElseThrow(() -> new NotFoundException("Dependency", dependencyId));
    }

    private static void rejectSelfLink(Long predecessorId, Long successorId) {
        if (predecessorId.equals(successorId)) {
            throw new SelfDependencyException();
        }
    }

    private void rejectCycle(Long projectId, Long predecessorId, Long successorId, long excludedDependencyId) {
        if (graphQueryRepository.isReachable(projectId, successorId, predecessorId, excludedDependencyId)) {
            List<String> path = graphQueryRepository.findPathTitles(projectId, successorId, predecessorId, excludedDependencyId);
            List<String> cycle = new ArrayList<>(path.size() + 1);
            cycle.add(path.getLast());
            cycle.addAll(path);
            throw new CyclicDependencyException(cycle);
        }
    }
}