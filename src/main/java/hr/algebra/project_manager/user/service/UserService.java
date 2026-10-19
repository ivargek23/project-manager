package hr.algebra.project_manager.user.service;

import hr.algebra.project_manager.user.domain.Role;
import hr.algebra.project_manager.user.repository.UserOption;
import hr.algebra.project_manager.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {
    private final UserRepository users;

    public List<UserOption> findLeadCandidates() {
        return users.findByRoleInAndEnabledTrueOrderByDisplayNameAsc(Role.CAN_BE_A_PROJECT_LEAD);
    }
}
