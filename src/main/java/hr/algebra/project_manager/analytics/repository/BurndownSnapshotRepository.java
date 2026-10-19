package hr.algebra.project_manager.analytics.repository;

import hr.algebra.project_manager.analytics.domain.BurndownSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BurndownSnapshotRepository extends JpaRepository<BurndownSnapshot, Long> {
}
