package hr.algebra.project_manager.project.service;

import hr.algebra.project_manager.common.exception.NotFoundException;
import hr.algebra.project_manager.common.WorkStatus;
import hr.algebra.project_manager.project.domain.Project;
import hr.algebra.project_manager.project.repository.ProjectRepository;
import hr.algebra.project_manager.user.domain.AppUser;
import hr.algebra.project_manager.user.domain.Role;
import hr.algebra.project_manager.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    private static final LocalDate START = LocalDate.of(2026, 10, 1);
    private static final LocalDate DUE = LocalDate.of(2026, 12, 15);

    @Mock
    private ProjectRepository projects;

    @Mock
    private UserRepository users;

    @InjectMocks
    private ProjectService service;

    private final AppUser lead = new AppUser("mchen", "Michael Chen", "hash", Role.PROJECT_LEAD);
    private final AppUser member = new AppUser("ahorvat", "Ana Horvat", "hash", Role.MEMBER);

    private ProjectCommand command(Long leadId) {
        return new ProjectCommand("Website Redesign", "New site", START, DUE, leadId, WorkStatus.TODO);
    }

    @Test
    void searchTrimsTheTextAndTreatsNullAsEmpty() {
        Pageable page = Pageable.unpaged();

        service.search("  web ", null, page);
        service.search(null, WorkStatus.DONE, page);

        verify(projects).search("web", null, page);
        verify(projects).search("", WorkStatus.DONE, page);
    }

    @Test
    void getThrowsNotFoundForUnknownProject() {
        when(projects.findWithLeadById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.get(99L)).isInstanceOf(NotFoundException.class);
    }

    @Test
    void createSavesProjectWithTheChosenLead() {
        when(users.findById(2L)).thenReturn(Optional.of(lead));
        when(projects.save(any(Project.class))).thenAnswer(call -> call.getArgument(0));

        service.create(command(2L));

        verify(projects).save(any(Project.class));
    }

    @Test
    void createRefusesAMemberAsLead() {
        when(users.findById(10L)).thenReturn(Optional.of(member));
        var cmd = command(10L);

        assertThatExceptionOfType(InvalidLeadException.class)
                .isThrownBy(() -> service.create(cmd))
                .withMessageContaining("lead");
        verify(projects, never()).save(any());
    }

    @Test
    void updateChangesTheFieldsOfTheLoadedProject() {
        Project project = new Project("Old name", START, DUE, lead);
        when(projects.findWithLeadById(1L)).thenReturn(Optional.of(project));
        when(users.findById(2L)).thenReturn(Optional.of(lead));

        service.update(1L, project.getVersion(), command(2L));

        assertThat(project.getName()).isEqualTo("Website Redesign");
        assertThat(project.getDescription()).isEqualTo("New site");
    }

    @Test
    void updateRefusesAStaleVersion() {
        Project project = new Project("Old name", START, DUE, lead);
        when(projects.findWithLeadById(1L)).thenReturn(Optional.of(project));
        var cmd = command(2L);

        assertThatThrownBy(() -> service.update(1L, 7L, cmd))
                .isInstanceOf(ObjectOptimisticLockingFailureException.class);
        assertThat(project.getName()).isEqualTo("Old name");
    }

    @Test
    void deleteThrowsNotFoundForUnknownProject() {
        when(projects.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(99L)).isInstanceOf(NotFoundException.class);
        verify(projects, never()).delete(any());
    }
}
