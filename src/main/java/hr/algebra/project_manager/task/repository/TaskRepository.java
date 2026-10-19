package hr.algebra.project_manager.task.repository;

import hr.algebra.project_manager.task.domain.Task;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskRepository extends JpaRepository<Task, Long> {
}
