package hr.algebra.project_manager.project.repository;

import hr.algebra.project_manager.project.domain.Project;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRepository extends JpaRepository<Project, Long> {
}
