package hr.algebra.project_manager.task.service;

import hr.algebra.project_manager.common.exception.NotFoundException;
import hr.algebra.project_manager.dependency.repository.EdgeView;
import hr.algebra.project_manager.dependency.repository.GraphQueryRepository;
import hr.algebra.project_manager.project.domain.Project;
import hr.algebra.project_manager.project.repository.ProjectMemberRepository;
import hr.algebra.project_manager.project.repository.ProjectRepository;
import hr.algebra.project_manager.task.domain.Subtask;
import hr.algebra.project_manager.task.domain.Task;
import hr.algebra.project_manager.task.repository.TaskRepository;
import hr.algebra.project_manager.user.domain.AppUser;
import hr.algebra.project_manager.user.repository.UserOption;
import hr.algebra.project_manager.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TaskService {

    private final TaskRepository tasks;
    private final GraphQueryRepository graph;
    private final ProjectMemberRepository projectMembers;
    private final ProjectRepository projects;
    private final UserRepository users;

    @PreAuthorize("hasRole('ADMIN') or @projectAccess.isLead(#projectId, authentication) or @projectAccess.isMember(#projectId, authentication)")
    public List<TaskRow> findRows(Long projectId) {
        Map<Long, Long> dependencyCounts = graph
                .findEdges(projectId)
                .stream()
                .collect(Collectors.groupingBy(EdgeView::successorId, Collectors.counting()));

        return tasks.findByProjectIdOrderById(projectId)
                .stream()
                .map(task -> new TaskRow(
                        task.getId(),
                        task.getTitle(),
                        task.getAssignee() == null ? null : task.getAssignee().getDisplayName(),
                        task.getEstimateDays(),
                        task.getStatus(),
                        dependencyCounts.getOrDefault(task.getId(), 0L),
                        task.getSubtasks().size(),
                        (int) task.getSubtasks().stream().filter(Subtask::isDone).count()
                )).toList();
    }

    @PreAuthorize("hasRole('ADMIN') or @projectAccess.isLead(#projectId, authentication) or @projectAccess.isMember(#projectId, authentication)")
    public Task get(Long projectId, Long taskId) {
        return tasks.findByIdAndProjectId(taskId, projectId).orElseThrow(() -> new NotFoundException("Task", taskId));
    }

    @PreAuthorize("@projectAccess.isLead(#projectId, authentication)")
    public List<UserOption> findAssigneeOptions(Long projectId) {
        return projectMembers.findMemberOptions(projectId);
    }

    @Transactional
    @PreAuthorize("@projectAccess.isLead(#projectId, authentication)")
    public Long createTask(Long projectId, TaskCommand cmd) {
        Project project = projects.findById(projectId).orElseThrow(() -> new NotFoundException("Project", projectId));
        Task task = new Task(
                project,
                cmd.title(),
                cmd.estimateDays()
        );
        task.setDescription(cmd.description());
        task.setStatus(cmd.status());
        task.setDueDate(cmd.dueDate());
        task.setAssignee(findAssignee(projectId, cmd.assigneeId()));
        return tasks.save(task).getId();
    }

    @Transactional
    @PreAuthorize("@projectAccess.isLead(#projectId, authentication)")
    public void updateTask(Long projectId, Long taskId, Long expectedVersion, TaskCommand cmd) {
        Task task = get(projectId, taskId);

        if (!Objects.equals(task.getVersion(), expectedVersion)) {
            throw new ObjectOptimisticLockingFailureException(Task.class, taskId);
        }

        task.setTitle(cmd.title());
        task.setEstimateDays(cmd.estimateDays());
        task.setStatus(cmd.status());
        task.setDescription(cmd.description());
        task.setDueDate(cmd.dueDate());
        task.setAssignee(findAssignee(projectId, cmd.assigneeId()));
    }

    @Transactional
    @PreAuthorize("@projectAccess.isLead(#projectId, authentication)")
    public void deleteTask(Long projectId, Long taskId) {
        Task task = get(projectId, taskId);
        tasks.delete(task);
    }

    @Transactional
    @PreAuthorize("@projectAccess.isLead(#projectId, authentication) or @projectAccess.isAssignee(#projectId, #taskId, authentication)")
    public void addSubtask(Long projectId, Long taskId, String title) {
        Task task = get(projectId, taskId);
        task.addSubtask(title.strip());
    }

    @Transactional
    @PreAuthorize("@projectAccess.isLead(#projectId, authentication) or @projectAccess.isAssignee(#projectId, #taskId, authentication)")
    public void setSubtaskDone(Long projectId, Long taskId, Long subtaskId, boolean done) {
        Task task = get(projectId, taskId);
        findSubtaskById(task, subtaskId).setDone(done);
    }

    @Transactional
    @PreAuthorize("@projectAccess.isLead(#projectId, authentication) or @projectAccess.isAssignee(#projectId, #taskId, authentication)")
    public void deleteSubtask(Long projectId, Long taskId, Long subtaskId) {
        Task task = get(projectId, taskId);
        task.removeSubtask(findSubtaskById(task, subtaskId));
    }

    private Subtask findSubtaskById(Task task, Long subtaskId) {
        return task.getSubtasks()
                .stream()
                .filter(subtask -> subtask.getId().equals(subtaskId))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Subtask", subtaskId));
    }

    @PreAuthorize("hasRole('ADMIN') or @projectAccess.isLead(#projectId, authentication) or @projectAccess.isMember(#projectId, authentication)")
    public List<SubtaskRow> findSubtasks(Long projectId, Long taskId) {
        return get(projectId, taskId)
                .getSubtasks()
                .stream()
                .map(subtask -> new SubtaskRow(subtask.getId(), subtask.getTitle(), subtask.isDone()))
                .toList();
    }

    private AppUser findAssignee(Long projectId, Long assigneeId) {

        if (assigneeId == null) return null;
        
        if (!projectMembers.existsByProjectIdAndUserId(projectId, assigneeId))
            throw new InvalidAssigneeException(assigneeId);
        return users.getReferenceById(assigneeId);
    }
}
