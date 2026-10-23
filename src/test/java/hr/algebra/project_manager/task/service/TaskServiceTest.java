package hr.algebra.project_manager.task.service;

import hr.algebra.project_manager.common.WorkStatus;
import hr.algebra.project_manager.common.exception.NotFoundException;
import hr.algebra.project_manager.dependency.repository.GraphQueryRepository;
import hr.algebra.project_manager.project.domain.Project;
import hr.algebra.project_manager.project.repository.ProjectMemberRepository;
import hr.algebra.project_manager.project.repository.ProjectRepository;
import hr.algebra.project_manager.task.domain.Subtask;
import hr.algebra.project_manager.task.domain.Task;
import hr.algebra.project_manager.task.repository.TaskRepository;
import hr.algebra.project_manager.user.domain.AppUser;
import hr.algebra.project_manager.user.domain.Role;
import hr.algebra.project_manager.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    private static final Long PROJECT_ID = 1L;
    private static final Long TASK_ID = 5L;
    private static final Long MEMBER_ID = 10L;
    private static final LocalDate DUE = LocalDate.of(2026, 11, 20);

    @Mock
    private TaskRepository tasks;

    @Mock
    private GraphQueryRepository graph;

    @Mock
    private ProjectMemberRepository projectMembers;

    @Mock
    private ProjectRepository projects;

    @Mock
    private UserRepository users;

    @InjectMocks
    private TaskService service;

    private final AppUser lead = new AppUser("pperic", "Pero Peric", "hash", Role.PROJECT_LEAD);
    private final AppUser member = new AppUser("ahorvat", "Ana Horvat", "hash", Role.MEMBER);
    private final Project project = new Project("Website Redesign",
            LocalDate.of(2026, 10, 1), LocalDate.of(2026, 12, 15), lead);

    private TaskCommand command(Long assigneeId, WorkStatus status) {
        return new TaskCommand("Homepage wireframes", "First draft", assigneeId, status, 5, DUE);
    }

    private Task existingTask() {
        Task task = new Task(project, "Old title", 3);
        when(tasks.findByIdAndProjectId(TASK_ID, PROJECT_ID)).thenReturn(Optional.of(task));
        return task;
    }

    // ---------------------------------------------------------------- create

    @Test
    void createSetsAllFieldsAndSaves() {
        when(projects.findById(PROJECT_ID)).thenReturn(Optional.of(project));
        when(projectMembers.existsByProjectIdAndUserId(PROJECT_ID, MEMBER_ID)).thenReturn(true);
        when(users.getReferenceById(MEMBER_ID)).thenReturn(member);
        when(tasks.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.createTask(PROJECT_ID, command(MEMBER_ID, WorkStatus.DONE));

        ArgumentCaptor<Task> saved = ArgumentCaptor.forClass(Task.class);
        verify(tasks).save(saved.capture());
        Task task = saved.getValue();
        assertThat(task.getProject()).isSameAs(project);
        assertThat(task.getTitle()).isEqualTo("Homepage wireframes");
        assertThat(task.getDescription()).isEqualTo("First draft");
        assertThat(task.getEstimateDays()).isEqualTo(5);
        assertThat(task.getDueDate()).isEqualTo(DUE);
        assertThat(task.getAssignee()).isSameAs(member);
        assertThat(task.getStatus()).isEqualTo(WorkStatus.DONE);
        assertThat(task.getCompletedAt()).isNotNull();
    }

    @Test
    void createRejectsAssigneeWhoIsNotAProjectMember() {
        when(projects.findById(PROJECT_ID)).thenReturn(Optional.of(project));
        when(projectMembers.existsByProjectIdAndUserId(PROJECT_ID, MEMBER_ID)).thenReturn(false);

        assertThatExceptionOfType(InvalidAssigneeException.class)
                .isThrownBy(() -> service.createTask(PROJECT_ID, command(MEMBER_ID, WorkStatus.TODO)));

        verify(tasks, never()).save(any());
    }

    @Test
    void createWithoutAssigneeSkipsTheMemberCheck() {
        when(projects.findById(PROJECT_ID)).thenReturn(Optional.of(project));
        when(tasks.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.createTask(PROJECT_ID, command(null, WorkStatus.TODO));

        verifyNoInteractions(projectMembers, users);
    }

    @Test
    void createInUnknownProjectThrowsNotFound() {
        when(projects.findById(99L)).thenReturn(Optional.empty());

        assertThatExceptionOfType(NotFoundException.class)
                .isThrownBy(() -> service.createTask(99L, command(null, WorkStatus.TODO)));

        verify(tasks, never()).save(any());
    }

    // ---------------------------------------------------------------- get / update / delete

    @Test
    void taskFromAnotherProjectThrowsNotFound() {
        when(tasks.findByIdAndProjectId(TASK_ID, 2L)).thenReturn(Optional.empty());

        assertThatExceptionOfType(NotFoundException.class)
                .isThrownBy(() -> service.get(2L, TASK_ID));
    }

    @Test
    void updateChangesAllFields() {
        Task task = existingTask();

        // a new Task has version null, so null is the matching expected version here
        service.updateTask(PROJECT_ID, TASK_ID, null, command(null, WorkStatus.IN_PROGRESS));

        assertThat(task.getTitle()).isEqualTo("Homepage wireframes");
        assertThat(task.getDescription()).isEqualTo("First draft");
        assertThat(task.getEstimateDays()).isEqualTo(5);
        assertThat(task.getDueDate()).isEqualTo(DUE);
        assertThat(task.getStatus()).isEqualTo(WorkStatus.IN_PROGRESS);
        assertThat(task.getAssignee()).isNull();
    }

    @Test
    void updateRejectsStaleVersion() {
        Task task = existingTask();

        assertThatExceptionOfType(ObjectOptimisticLockingFailureException.class)
                .isThrownBy(() -> service.updateTask(PROJECT_ID, TASK_ID, 5L, command(null, WorkStatus.TODO)));

        assertThat(task.getTitle()).isEqualTo("Old title");
    }

    @Test
    void deleteRemovesTheTask() {
        Task task = existingTask();

        service.deleteTask(PROJECT_ID, TASK_ID);

        verify(tasks).delete(task);
    }

    // ---------------------------------------------------------------- subtasks

    @Test
    void addSubtaskStripsTheTitle() {
        Task task = existingTask();

        service.addSubtask(PROJECT_ID, TASK_ID, "  Write copy  ");

        assertThat(task.getSubtasks())
                .singleElement()
                .extracting(Subtask::getTitle)
                .isEqualTo("Write copy");
    }

    @Test
    void setSubtaskDoneMarksTheSubtask() {
        Task task = existingTask();
        Subtask subtask = subtaskWithId(task, "Write copy", 7L);

        service.setSubtaskDone(PROJECT_ID, TASK_ID, 7L, true);

        assertThat(subtask.isDone()).isTrue();
    }

    @Test
    void subtaskOfAnotherTaskThrowsNotFound() {
        Task task = existingTask();
        subtaskWithId(task, "Write copy", 7L);

        assertThatExceptionOfType(NotFoundException.class)
                .isThrownBy(() -> service.setSubtaskDone(PROJECT_ID, TASK_ID, 99L, true));
    }

    @Test
    void deleteSubtaskRemovesItFromTheTask() {
        Task task = existingTask();
        subtaskWithId(task, "Write copy", 7L);
        subtaskWithId(task, "Pick images", 8L);

        service.deleteSubtask(PROJECT_ID, TASK_ID, 7L);

        assertThat(task.getSubtasks())
                .extracting(Subtask::getTitle)
                .containsExactly("Pick images");
    }

    @Test
    void findSubtasksReturnsRowsInOrder() {
        Task task = existingTask();
        subtaskWithId(task, "Write copy", 7L).setDone(true);
        subtaskWithId(task, "Pick images", 8L);

        assertThat(service.findSubtasks(PROJECT_ID, TASK_ID)).containsExactly(
                new SubtaskRow(7L, "Write copy", true),
                new SubtaskRow(8L, "Pick images", false));
    }

    // In a unit test nothing is saved, so ids stay null. Hibernate sets them when it
    // loads an entity; here the test does the same with ReflectionTestUtils.
    private static Subtask subtaskWithId(Task task, String title, Long id) {
        Subtask subtask = task.addSubtask(title);
        ReflectionTestUtils.setField(subtask, "id", id);
        return subtask;
    }
}