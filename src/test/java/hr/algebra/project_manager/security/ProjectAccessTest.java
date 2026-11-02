package hr.algebra.project_manager.security;

import hr.algebra.project_manager.project.repository.ProjectMemberRepository;
import hr.algebra.project_manager.project.repository.ProjectRepository;
import hr.algebra.project_manager.task.repository.TaskRepository;
import hr.algebra.project_manager.user.domain.Role;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectAccessTest {

    @Mock
    private ProjectMemberRepository projectMemberRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private ProjectAccess projectAccess;

    private static final AppUserPrincipal USER = new AppUserPrincipal(2L, "lead", "hash", Role.PROJECT_LEAD, true);

    private Authentication authenticatedAs(AppUserPrincipal principal) {
        return new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
    }

    @Test
    void isLead_ifUserIsNotAuthenticated_returnsFalse() {
        Authentication anonymousUser = new UsernamePasswordAuthenticationToken("anonymousUser", null, List.of());

        assertThat(projectAccess.isLead(1L, anonymousUser)).isFalse();
        verifyNoInteractions(projectRepository);
    }

    @Test
    void isLead_ifUserLeadsProject_returnsTrue() {
        when(projectRepository.existsByIdAndLeadId(1L, 2L)).thenReturn(true);

        assertThat(projectAccess.isLead(1L, authenticatedAs(USER))).isTrue();
    }

    @Test
    void isLead_ifUserDoesNotLeadProject_returnsFalse() {
        when(projectRepository.existsByIdAndLeadId(3L, 2L)).thenReturn(false);

        assertThat(projectAccess.isLead(3L, authenticatedAs(USER))).isFalse();
    }

    @Test
    void isMember_ifUserIsMemberOfProject_returnsTrue() {
        when(projectMemberRepository.existsByProjectIdAndUserId(1L, 2L)).thenReturn(true);

        assertThat(projectAccess.isMember(1L, authenticatedAs(USER))).isTrue();
    }

    @Test
    void isAssignee_ifUserIsAssignedToTaskInSameProject_returnsTrue() {
        when(taskRepository.existsByIdAndProjectIdAndAssigneeId(10L, 1L, 2L)).thenReturn(true);

        assertThat(projectAccess.isAssignee(1L, 10L, authenticatedAs(USER))).isTrue();
    }
}
