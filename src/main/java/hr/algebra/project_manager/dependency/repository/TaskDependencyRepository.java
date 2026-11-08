package hr.algebra.project_manager.dependency.repository;

import hr.algebra.project_manager.dependency.domain.TaskDependency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TaskDependencyRepository extends JpaRepository<TaskDependency, Long> {
    Optional<TaskDependency> findByIdAndProjectId(Long id, Long projectId);

    boolean existsByPredecessorIdAndSuccessorId(Long predecessorId, Long successorId);

    boolean existsByPredecessorIdAndSuccessorIdAndIdNot(Long predecessorId, Long successorId, Long id);

    @Query(TaskDependencyQueries.FIND_ROWS)
    List<DependencyRow> findRows(@Param("projectId") Long projectId);

    @Query(TaskDependencyQueries.FIND_ROW)
    Optional<DependencyRow> findRow(@Param("projectId") Long projectId, @Param("id") Long id);

    @Query(TaskDependencyQueries.FIND_PREDECESSOR_ROWS)
    List<DependencyRow> findPredecessorRows(@Param("projectId") Long projectId, @Param("taskId") Long taskId);
}
