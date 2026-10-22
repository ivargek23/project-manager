package hr.algebra.project_manager.project.repository;

import hr.algebra.project_manager.project.domain.ProjectMember;
import hr.algebra.project_manager.user.repository.UserOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProjectMemberRepository extends JpaRepository<ProjectMember, Long> {
    boolean existsByProjectIdAndUserId(Long projectId, Long userId);

    @Query("""
        SELECT NEW hr.algebra.project_manager.user.repository.UserOption(m.user.id, m.user.displayName)
        FROM ProjectMember m
        WHERE m.project.id = :projectId AND m.user.enabled = true
        ORDER BY m.user.displayName
        """)
    List<UserOption> findMemberOptions(@Param("projectId") Long projectId);
}
