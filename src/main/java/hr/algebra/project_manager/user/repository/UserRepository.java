package hr.algebra.project_manager.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import hr.algebra.project_manager.user.domain.AppUser;

import java.util.Optional;

public interface UserRepository extends JpaRepository<AppUser, Long> {

    Optional<AppUser> findByUsername(String username);
}
