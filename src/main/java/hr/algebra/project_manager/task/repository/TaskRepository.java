package hr.algebra.project_manager.task.repository;

import hr.algebra.project_manager.task.domain.Task;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TaskRepository extends JpaRepository<Task, Long> {

    Optional<Task> findByIdAndProjectId(Long id, Long projectId);
    List<Task> findByProjectIdOrderById(Long projectId);
    boolean existsByIdAndProjectIdAndAssigneeId(Long taskId, Long projectId, Long assigneeId);
}
