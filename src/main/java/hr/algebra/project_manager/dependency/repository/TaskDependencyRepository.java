package hr.algebra.project_manager.dependency.repository;

import hr.algebra.project_manager.dependency.domain.TaskDependency;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskDependencyRepository extends JpaRepository<TaskDependency, Long> {
}
