package hr.algebra.project_manager.task.service;

import hr.algebra.project_manager.dependency.repository.EdgeView;
import hr.algebra.project_manager.dependency.repository.GraphQueryRepository;
import hr.algebra.project_manager.task.domain.Subtask;
import hr.algebra.project_manager.task.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TaskService {

    private final TaskRepository tasks;
    private final GraphQueryRepository graph;

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
}
