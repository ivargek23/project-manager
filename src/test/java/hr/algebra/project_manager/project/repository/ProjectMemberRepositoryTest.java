package hr.algebra.project_manager.project.repository;

import hr.algebra.project_manager.TestcontainersConfiguration;
import hr.algebra.project_manager.user.domain.AppUser;
import hr.algebra.project_manager.user.repository.UserOption;
import hr.algebra.project_manager.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestcontainersConfiguration.class)
class ProjectMemberRepositoryTest {

    @Autowired
    private ProjectMemberRepository members;

    @Autowired
    private UserRepository users;

    @Test
    void memberOptionsContainOnlyThisProjectsMembersSortedByName() {
        assertThat(members.findMemberOptions(2L))
                .extracting(UserOption::displayName)
                .containsExactly("Ana Anic", "Clan Tima", "Marija Maric", "Petra Kovac");
    }

    @Test
    void disabledUserIsNotOfferedAsAssignee() {
        AppUser user = users.findById(12L).orElseThrow();
        user.setEnabled(false);
        users.saveAndFlush(user);

        assertThat(members.findMemberOptions(2L))
                .extracting(UserOption::displayName)
                .doesNotContain("Clan Tima");
    }

    @Test
    void existsChecksMembershipInTheGivenProject() {
        assertThat(members.existsByProjectIdAndUserId(2L, 11L)).isTrue();
        assertThat(members.existsByProjectIdAndUserId(1L, 11L)).isFalse();
    }
}