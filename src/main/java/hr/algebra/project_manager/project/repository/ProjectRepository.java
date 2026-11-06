package hr.algebra.project_manager.project.repository;

import hr.algebra.project_manager.common.WorkStatus;
import hr.algebra.project_manager.project.domain.Project;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ProjectRepository extends JpaRepository<Project, Long> {

    @Query(value = """
        SELECT NEW hr.algebra.project_manager.project.repository.ProjectSummary(
                        p.id, p.name, l.displayName, p.dueDate, p.status, COUNT (t.id))
                        FROM Project p
                        JOIN p.lead l
                        LEFT JOIN p.tasks t
                        WHERE (:status IS NULL OR p.status = :status)
                        AND lower(p.name) LIKE lower(concat('%', :query, '%'))
                        GROUP BY p.id, p.name, l.displayName, p.dueDate, p.status
        """,
        countQuery = """
        SELECT count(p)
        FROM Project p
        WHERE (:status IS NULL OR p.status = :status)
        AND lower(p.name) LIKE lower(concat('%', :query, '%'))
        """)
    Page<ProjectSummary> search(@Param("query") String query,
                                @Param("status") WorkStatus status,
                                Pageable pageable);

    @Query(value = """
    SELECT NEW hr.algebra.project_manager.project.repository.ProjectSummary(
                    p.id, p.name, l.displayName, p.dueDate, p.status, COUNT (t.id))
                    FROM Project p
                    JOIN p.lead l
                    LEFT JOIN p.tasks t
                    WHERE (:status IS NULL OR p.status = :status)
                    AND lower(p.name) LIKE lower(concat('%', :query, '%'))
                    AND (l.id = :userId
                         OR EXISTS (SELECT pm FROM ProjectMember pm
                                    WHERE pm.project = p AND pm.user.id = :userId))
                    GROUP BY p.id, p.name, l.displayName, p.dueDate, p.status
    """,
            countQuery = """
    SELECT count(p)
    FROM Project p
    JOIN p.lead l
    WHERE (:status IS NULL OR p.status = :status)
    AND lower(p.name) LIKE lower(concat('%', :query, '%'))
    AND (l.id = :userId
         OR EXISTS (SELECT pm FROM ProjectMember pm
                    WHERE pm.project = p AND pm.user.id = :userId))
    """)
    Page<ProjectSummary> searchVisibleTo(@Param("userId") Long userId,
                                         @Param("query") String query,
                                         @Param("status") WorkStatus status,
                                         Pageable pageable);

    @EntityGraph(attributePaths = "lead")
    Optional<Project> findWithLeadById(Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Project p WHERE p.id=:id")
    Optional<Project> findByIdForUpdate(@Param("id") Long id);

    boolean existsByIdAndLeadId(Long id, Long leadId);
}
