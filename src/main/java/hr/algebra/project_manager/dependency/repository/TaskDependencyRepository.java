package hr.algebra.project_manager.dependency.repository;

import hr.algebra.project_manager.dependency.domain.TaskDependency;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TaskDependencyRepository extends JpaRepository<TaskDependency, Long> {
    Optional<TaskDependency> findByIdAndProjectId(Long id, Long projectId);

    boolean existsByPredecessorIdAndSuccessorId(Long predecessorId, Long successorId);

    boolean existsByPredecessorIdAndSuccessorIdAndIdNot(Long predecessorId, Long successorId, Long id);
}
