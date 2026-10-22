package hr.algebra.project_manager.user.repository;

import hr.algebra.project_manager.user.domain.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import hr.algebra.project_manager.user.domain.AppUser;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<AppUser, Long> {

    Optional<AppUser> findByUsername(String username);
    List<UserOption> findByRoleInAndEnabledTrueOrderByDisplayNameAsc(Collection<Role> roles);
}
